package com.example.banktest.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.banktest.R;
import com.example.banktest.databinding.ActivityChatBinding;
import com.example.banktest.helpers.Message;
import com.example.banktest.helpers.MessageAdapter;
import com.example.banktest.helpers.SpeechRecognitionHelper;
import com.example.banktest.helpers.TTSHelper;
import com.example.banktest.viewmodel.ChatViewModel;


public class ChatActivity extends BaseActivity implements SpeechRecognitionHelper.SpeechResultListener {
    private ChatViewModel chatViewModel;
    private ActivityChatBinding binding;
    private MessageAdapter messageAdapter;
    private SpeechRecognizer speechRecognizer;
    private SpeechRecognitionHelper speechHelper;
    private TTSHelper ttsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);

        // Initialize ViewModel
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // Set ViewModel in binding
        binding.setViewModel(chatViewModel);

        // Ensure LiveData is lifecycle aware
        binding.setLifecycleOwner(this);

        // Set up RecyclerView
        messageAdapter = new MessageAdapter(chatViewModel.getMessageListLiveData().getValue());
        binding.recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(layoutManager);

        // Observe message list changes
        chatViewModel.getMessageListLiveData().observe(this, messages -> {
            messageAdapter.setMessageList(messages);
            binding.recyclerView.scrollToPosition(messages.size() - 1);
        });

        // Observe scroll position changes
        chatViewModel.getScrollToPositionLiveData().observe(this, position -> {
            if (position != null) {
                binding.recyclerView.smoothScrollToPosition(position);
            }
        });

        // Observe loading state
        chatViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            binding.loadingText.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe button enablement state
        chatViewModel.getIsButtonsEnabledLiveData().observe(this, isEnabled -> {
            binding.sendBtn.setEnabled(isEnabled);
            binding.talkBtn.setEnabled(isEnabled);
        });

        // Observe clear input field state
        chatViewModel.getClearInputFieldLiveData().observe(this, clearInput -> {
            if (clearInput) {
                binding.messageEditText.setText("");
                chatViewModel.clearInputFieldDone(); // Notify ViewModel that input field has been cleared
            }
        });

        // Observe toast messages
        chatViewModel.getToastMessageLiveData().observe(this, toastMessage -> {
            if (toastMessage != null) {
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe welcome view visibility state
        chatViewModel.getIsWelcomeVisibleLiveData().observe(this, isVisible -> {
            binding.welcomeText.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        });

        // Observe Skip button visibility state
        chatViewModel.getIsSkipBtnVisibleLiveData().observe(this, isVisible -> {
            binding.skipBtn.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        });

        // Initialize helpers that require Activity context
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechHelper = new SpeechRecognitionHelper(this, this, binding.listeningText);
        ttsHelper = new TTSHelper(this, null);

        // Observe UI BOT response
        chatViewModel.getUiBOTResponseLiveData().observe(this, response -> {
            if (response != null) {
                addToChatUI(response, Message.SENT_BY_BOT);
            }
        });

        // Observe UI USER response
        chatViewModel.getUiUSERResponseLiveData().observe(this, response -> {
            if (response != null) {
                addToChatUI(response, Message.SENT_BY_ME);
            }
        });

        // Observe speakLiveData
        chatViewModel.getSpeakLiveData().observe(this, text -> {
            if (text != null && !text.isEmpty()) {
                chatViewModel.getLatchLiveData().observe(this, latch -> {
                    if (latch != null) {
                        ttsHelper.speakOut(text, latch);
                    } else {
                        ttsHelper.speakOut_simple(text);
                    }
                });
            }
        });

        // Set up button click listeners
        binding.sendBtn.setOnClickListener(v -> {
            chatViewModel.handleUserInput(binding.messageEditText.getText().toString().trim());
            binding.messageEditText.setText("");  // Clear the EditText after sending the message
        });

        binding.talkBtn.setOnClickListener(v -> {
            if (ttsHelper.isSpeaking()) {
                ttsHelper.stopSpeaking();
            }
            speechHelper.startListening();
        });
        binding.skipBtn.setOnClickListener(v -> {
            if (ttsHelper.isSpeaking()) {
                ttsHelper.stopSpeaking();
            }
            chatViewModel.removeLastChunkAndDisplayEntireMessage(chatViewModel.getCompleteResponse());
        });

        // Check for audio recording permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            speechHelper.checkPermission();
        }

        // Call retrieveAndCreate method from ViewModel
        chatViewModel.retrieveAndCreate("asst_OJZkZLb9OiMTZrflC4HES2tr");
    }

    private void addToChatUI(String input, String sentBy) {
        if (!input.trim().isEmpty()) { // Ensure the input is not empty after trimming
            runOnUiThread(() -> {
                int insertPosition = chatViewModel.getMessageListLiveData().getValue().size();
                chatViewModel.getMessageListLiveData().getValue().add(new Message(input.trim(), sentBy));
                messageAdapter.notifyItemInserted(insertPosition);
                binding.recyclerView.smoothScrollToPosition(insertPosition);
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
        ttsHelper.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        speechHelper.onRequestPermissionsResult(requestCode, grantResults);
    }

    @Override
    public void onSpeechResult(String text) {
        chatViewModel.handleUserInput(text);
    }
}