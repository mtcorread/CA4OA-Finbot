package com.example.banktest.helpers;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ThemeManager {
    private static ThemeManager instance;
    private MutableLiveData<Boolean> darkThemeEnabled = new MutableLiveData<>();
    private SharedPreferences sharedPreferences;

    private ThemeManager(Context context) {
        sharedPreferences = context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE);
        darkThemeEnabled.setValue(sharedPreferences.getBoolean("use_dark_theme", false));
    }

    public static synchronized ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context.getApplicationContext());
        }
        return instance;
    }

    public LiveData<Boolean> getDarkThemeEnabled() {
        return darkThemeEnabled;
    }

    public void toggleTheme() {
        boolean currentTheme = sharedPreferences.getBoolean("use_dark_theme", false);
        Log.d("ThemeManager", "Current theme before toggle (is Dark theme?): " + currentTheme);

        // Toggle the theme preference and update SharedPreferences
        sharedPreferences.edit().putBoolean("use_dark_theme", !currentTheme).apply();
        Log.d("ThemeManager", "Theme toggled to (is Dark theme?): " + !currentTheme);

        // Notify observers about the change
        darkThemeEnabled.setValue(!currentTheme);

    }


}

