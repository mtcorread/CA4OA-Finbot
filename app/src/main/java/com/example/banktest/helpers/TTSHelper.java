package com.example.banktest.helpers;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class TTSHelper {
    private TextToSpeech tts;
    private final Context context;
    private final Runnable onInitialized;

    public TTSHelper(Context context, Runnable onInitialized){
        this.context = context;
        this.onInitialized = onInitialized;
        initializeTTS();
    }

    public boolean isSpeaking() {
        return tts != null && tts.isSpeaking();
    }

    // Method to stop TTS speaking
    public void stopSpeaking() {
        if (tts != null) {
            tts.stop();
            // Optionally, you could also call tts.shutdown() if you want to release the TTS resources,
            // but typically you'd only do this when you're completely done with TTS.
        }
    }
    private void initializeTTS() {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.UK);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "This Language is not supported");
                } else {
                    if (onInitialized != null) {
                        onInitialized.run(); // Run the callback once TTS is initialized
                    }
                }
            } else {
                Log.e("TTS", "Initialization Failed!");
            }
        });
    }

    public void speakOut(String text, CountDownLatch latch) {
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, String.valueOf(text.hashCode()));
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // Called when TTS starts speaking.
            }

            @Override
            public void onDone(String utteranceId) {
                // Called when TTS has finished speaking.
                latch.countDown(); // Decrease the latch count to allow the thread to proceed.
            }

            @Override
            public void onError(String utteranceId) {
                // Called on TTS error.
                latch.countDown(); // Ensure the latch count is decreased even on error.
            }
        });
    }

    public void speakOut_simple(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
