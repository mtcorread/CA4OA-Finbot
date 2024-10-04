package com.example.banktest.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.banktest.R;
import com.example.banktest.helpers.Message;
import com.example.banktest.viewmodel.TransferViewModel;
import com.example.banktest.helpers.MessageAdapter;
import com.example.banktest.databinding.ActivityTransferBinding;
import com.example.banktest.helpers.SpeechRecognitionHelper;
import com.example.banktest.helpers.TTSHelper;

public class TransferActivity extends BaseActivity implements SpeechRecognitionHelper.SpeechResultListener {
    private TransferViewModel transferViewModel;
    private ActivityTransferBinding binding;
    private MessageAdapter messageAdapter;
    private SpeechRecognizer speechRecognizer;
    private SpeechRecognitionHelper speechHelper;
    private TTSHelper ttsHelper;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_transfer);

        // Initialize ViewModel
        transferViewModel = new ViewModelProvider(this).get(TransferViewModel.class);

        // Set ViewModel in binding
        binding.setViewModel(transferViewModel);

        // Ensure LiveData is lifecycle aware
        binding.setLifecycleOwner(this);

        // Initialize TTS and SpeechRecognizer
        ttsHelper = new TTSHelper(this, this::onTTSInitialized);

        // Set up RecyclerView
        messageAdapter = new MessageAdapter(transferViewModel.getMessageListLiveData().getValue());
        binding.recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(layoutManager);

        // Observe navigation event
        transferViewModel.getNavigateToMainActivity().observe(this, navigate -> {
            if (navigate) {
                navigateToMainActivity();
                transferViewModel.doneNavigating();
            }
        });

        // Observe message list changes
        transferViewModel.getMessageListLiveData().observe(this, messages -> {
            messageAdapter.notifyDataSetChanged();
            if (messages != null && !messages.isEmpty()) {
                binding.recyclerView.smoothScrollToPosition(messages.size() - 1);
            }
        });

        // Observe scroll position changes
        transferViewModel.getScrollToPositionLiveData().observe(this, position -> {
            if (position != null) {
                binding.recyclerView.smoothScrollToPosition(position);
            }
        });

        // Observe loading state
        transferViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            binding.loadingText.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe Send/Speak buttons visibility
        transferViewModel.getSendSpeakButtonsVisibleLiveData().observe(this, isVisible -> {
            binding.sendBtn.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            binding.talkBtn.setVisibility(isVisible ? View.VISIBLE : View.GONE  );
        });

        // Observe Send/Speak button enablement state
        transferViewModel.getIsSendSpeakButtonsEnabledLiveData().observe(this, isEnabled -> {
            binding.sendBtn.setEnabled(isEnabled);
            binding.talkBtn.setEnabled(isEnabled);
        });

        // Observe Yes/No buttons visibility
        transferViewModel.getYesNoButtonsVisibleLiveData().observe(this, isVisible -> {
            binding.yesBtn.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            binding.noBtn.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        });

        // Observe Yes/No buttons enable state
        transferViewModel.getYesNoButtonsEnabledLiveData().observe(this, isEnabled -> {
            binding.yesBtn.setEnabled(isEnabled);
            binding.noBtn.setEnabled(isEnabled);
        });

        // Observe Input Text Box visibility
        transferViewModel.getInputTextBoxVisibleLiveData().observe(this, isVisible -> {
            binding.messageEditText.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        });

        // Observe clear input field state
        transferViewModel.getClearInputFieldLiveData().observe(this, clearInput -> {
            if (clearInput) {
                binding.messageEditText.setText("");
                transferViewModel.clearInputFieldDone(); // Notify ViewModel that input field has been cleared
            }
        });

        // Observe UI BOT response
        transferViewModel.getUiBOTResponseLiveData().observe(this, response -> {
            if (response != null) {
                addToChatUI(response, Message.SENT_BY_BOT);
            }
        });

        // Observe UI USER response
        transferViewModel.getUiUSERResponseLiveData().observe(this, response -> {
            if (response != null) {
                addToChatUI(response, Message.SENT_BY_ME);
            }
        });

        // Observe speakLiveData
        transferViewModel.getSpeakLiveData().observe(this, text -> {
            if (text != null && !text.isEmpty()) {
                transferViewModel.getLatchLiveData().observe(this, latch -> {
                    if (latch != null) {
                        ttsHelper.speakOut(text, latch);
                    } else {
                        ttsHelper.speakOut_simple(text);
                    }
                });
            }
        });

        // Observe input type changes
        transferViewModel.getInputTypeLiveData().observe(this, inputType -> {
            if (inputType != null) {
                binding.messageEditText.setInputType(inputType);
                if (inputType != InputType.TYPE_NULL) {
                    showKeyboardAndRequestFocus(binding.messageEditText);
                } else {
                    hideKeyboard();
                }
            }
        });

        // Initialize helpers that require Activity context
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechHelper = new SpeechRecognitionHelper(this, this, binding.listeningText);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        // Check for audio recording permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            speechHelper.checkPermission();
        }

        binding.sendBtn.setOnClickListener(v -> {
            transferViewModel.handleUserInput(binding.messageEditText.getText().toString().trim());
            binding.messageEditText.setText("");  // Clear the EditText after sending the message
        });

        binding.talkBtn.setOnClickListener(v -> {
            if (ttsHelper.isSpeaking()) {
                ttsHelper.stopSpeaking();
            }
            speechHelper.startListening();
        });

        // Set up Yes button
        binding.yesBtn.setOnClickListener(v -> transferViewModel.handleUserInput("yes"));

        // Set up No button
        binding.noBtn.setOnClickListener(v -> transferViewModel.handleUserInput("no"));
    }

    private void navigateToMainActivity() {
        Intent mainIntent = new Intent(TransferActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void onTTSInitialized() {
        transferViewModel.setLoading(false);
        transferViewModel.setSendSpeakButtonsEnabledLiveData(true);
        // Trigger the first bot message after TTS is initialized
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            transferViewModel.handleUserInput(""); // Trigger the first question
        }, 0);
    }

    private void addToChatUI(String input, String sentBy) {
        if (!input.trim().isEmpty()) { // Ensure the input is not empty after trimming
            runOnUiThread(() -> {
                int insertPosition = transferViewModel.getMessageListLiveData().getValue().size();
                transferViewModel.getMessageListLiveData().getValue().add(new Message(input.trim(), sentBy));
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
        transferViewModel.handleUserInput(text);
    }

    private void showKeyboardAndRequestFocus(EditText editText) {
        editText.requestFocus();
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        imm.hideSoftInputFromWindow(binding.messageEditText.getWindowToken(), 0);
    }
}
