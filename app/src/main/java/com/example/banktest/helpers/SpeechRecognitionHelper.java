package com.example.banktest.helpers;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechRecognitionHelper {
    private final SpeechRecognizer speechRecognizer;
    private final Intent speechRecognizerIntent;
    private final TextView listeningIndicator;
    private final Activity activity;
    public static final int RecordAudioRequestCode = 1;

    public SpeechRecognitionHelper(Activity activity, SpeechResultListener resultListener, TextView listeningIndicator) {
        this.listeningIndicator = listeningIndicator;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        this.activity = activity;
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {}

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {}

            @Override
            public void onBufferReceived(byte[] bytes) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                // Run on UI thread to ensure UI operations are safe
                activity.runOnUiThread(() -> {
                    if (listeningIndicator != null) {
                        listeningIndicator.setVisibility(View.GONE); // Hide the "Listening..." TextView
                    }

                    // Optional: Provide feedback to the user about the error
                    if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                        Toast.makeText(activity, "No speech input detected", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResults(Bundle bundle) {
                if (listeningIndicator != null) {
                    activity.runOnUiThread(() -> listeningIndicator.setVisibility(View.GONE));
                }
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null && resultListener != null) {
                    resultListener.onSpeechResult(data.get(0)); // Use the callback to send the result
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {}

            @Override
            public void onEvent(int i, Bundle bundle) {}
        });
    }

    public void startListening() {
        if (listeningIndicator != null) {
            activity.runOnUiThread(() -> listeningIndicator.setVisibility(View.VISIBLE));
        }
        speechRecognizer.startListening(speechRecognizerIntent);

    }


    public void stopListening() {
        // Handler from android.os package
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // This code will be executed after the specified delay
            if (speechRecognizer != null) {
                speechRecognizer.stopListening();
            }
        }, 800); // Delay in milliseconds, e.g., 3000ms for 3 seconds
    }

    public interface SpeechResultListener {
        void onSpeechResult(String text);
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
        }
    }

    // Call this method from the Activity's onRequestPermissionsResult
    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}