package com.example.banktest.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import androidx.lifecycle.Observer;

import com.example.banktest.R;
import com.example.banktest.helpers.FontManager;
import com.example.banktest.helpers.ThemeManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends BaseActivity {
    private RadioGroup radioGroupFontSize;
    private SwitchMaterial themeToggleSwitch, ttsSwitch;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        themeToggleSwitch = findViewById(R.id.themeToggleSwitch);
        radioGroupFontSize = findViewById(R.id.radioGroupFontSize);
        ttsSwitch = findViewById(R.id.TTSToggleSwitch);
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        // Load saved preferences
        FontManager fontManager = FontManager.getInstance(this);
        String fontSize = fontManager.getSavedFontSize();

        if ("Large".equals(fontSize)) {
            radioGroupFontSize.check(R.id.radioButtonLarge);
        } else if ("ExtraLarge".equals(fontSize)) {
            radioGroupFontSize.check(R.id.radioButtonExtraLarge);
        } else {
            radioGroupFontSize.check(R.id.radioButtonNormal);
        }

        radioGroupFontSize.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonLarge) {
                fontManager.setFontSize("Large");
            } else if (checkedId == R.id.radioButtonExtraLarge) {
                fontManager.setFontSize("ExtraLarge");
            } else {
                fontManager.setFontSize("Normal");
            }
        });

        // Load the saved TTS preference
        boolean isTtsEnabled = sharedPreferences.getBoolean("isTtsEnabled", true);
        ttsSwitch.setChecked(isTtsEnabled);

        // Set a listener to save the preference when the switch is toggled
        ttsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isTtsEnabled", isChecked);
            editor.apply();
        });

        ThemeManager themeManager = ThemeManager.getInstance(getApplicationContext());

        // Observe the LiveData to update the switch when the theme changes
        themeManager.getDarkThemeEnabled().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDarkTheme) {
                themeToggleSwitch.setOnCheckedChangeListener(null);  // Remove the listener temporarily
                themeToggleSwitch.setChecked(isDarkTheme);
                themeToggleSwitch.setOnCheckedChangeListener(switchChangeListener);  // Add the listener back
            }
        });

        // Set the initial state of the switch
        boolean isDarkTheme = themeManager.getDarkThemeEnabled().getValue() != null && themeManager.getDarkThemeEnabled().getValue();
        themeToggleSwitch.setChecked(isDarkTheme);
        themeToggleSwitch.setOnCheckedChangeListener(switchChangeListener);  // Add the listener back
    }

    // Define the listener separately
    private final CompoundButton.OnCheckedChangeListener switchChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ThemeManager.getInstance(getApplicationContext()).toggleTheme();
        }
    };

}