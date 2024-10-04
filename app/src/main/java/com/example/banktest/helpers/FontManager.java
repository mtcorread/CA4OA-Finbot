package com.example.banktest.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class FontManager {
    private static FontManager instance;
    private SharedPreferences sharedPreferences;
    private MutableLiveData<String> fontSizeLiveData;

    private static final String FONT_SIZE_KEY = "fontSize";
    private static final String PREFS_NAME = "AppSettings";

    private FontManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        fontSizeLiveData = new MutableLiveData<>();
        fontSizeLiveData.setValue(getSavedFontSize());
    }

    public static FontManager getInstance(Context context) {
        if (instance == null) {
            instance = new FontManager(context);
        }
        return instance;
    }

    public void setFontSize(String fontSize) {
        sharedPreferences.edit().putString(FONT_SIZE_KEY, fontSize).apply();
        fontSizeLiveData.setValue(fontSize);
    }

    public String getSavedFontSize() {
        return sharedPreferences.getString(FONT_SIZE_KEY, "Normal");
    }

    public LiveData<String> getFontSizeLiveData() {
        return fontSizeLiveData;
    }
}

