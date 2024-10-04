package com.example.banktest.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import com.example.banktest.R;
import com.example.banktest.viewmodel.MainViewModel;
import com.example.banktest.databinding.ActivityMainBinding;

public class MainActivity extends BaseActivity {
    private MainViewModel mainViewModel;
    private ActivityMainBinding binding;
    boolean isPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Initialize ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Set ViewModel in binding
        binding.setViewModel(mainViewModel);

        // Ensure LiveData is lifecycle aware
        binding.setLifecycleOwner(this);

        // Observe pension click event
        mainViewModel.getPensionClickEvent().observe(this, clicked -> {
            if (clicked != null && clicked) {
                Toast.makeText(this, "Currently unavailable", Toast.LENGTH_SHORT).show();
                mainViewModel.resetPensionClickEvent(); // Reset the event
            }
        });

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float density = displayMetrics.density;
        int widthDp = (int) (displayMetrics.widthPixels / density);
        int heightDp = (int) (displayMetrics.heightPixels / density);
        Log.d("ScreenSizeInfo", "Screen Width: " + widthDp + "dp, Screen Height: " + heightDp + "dp");

        isPhone = widthDp <= 600;

        binding.pensionCard.setOnClickListener(v -> Toast.makeText(this, "Currently unavailable", Toast.LENGTH_SHORT).show());

        // Observe navigation events
        mainViewModel.navigateToTransfer.observe(this, navigate -> {
            if (navigate != null && navigate) {
                startActivity(new Intent(MainActivity.this, TransferActivity.class));
                mainViewModel.doneNavigatingToTransfer(); // Reset navigation state
            }
        });

        mainViewModel.navigateToBalance.observe(this, navigate -> {
            if (navigate != null && navigate) {
                Intent balanceIntent;
                if (isPhone) {
                    balanceIntent = new Intent(MainActivity.this, BalanceActivity.class);
                } else {
                    balanceIntent = new Intent(MainActivity.this, BalanceActivity_bigScreen.class);
                }
                startActivity(balanceIntent);
                mainViewModel.doneNavigatingToBalance(); // Reset navigation state
            }
        });

        mainViewModel.navigateToChat.observe(this, navigate -> {
            if (navigate != null && navigate) {
                startActivity(new Intent(MainActivity.this, ChatActivity.class));
                mainViewModel.doneNavigatingToChat(); // Reset navigation state
            }
        });

        mainViewModel.navigateToSettings.observe(this, navigate -> {
            if (navigate != null && navigate) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                mainViewModel.doneNavigatingToSettings(); // Reset navigation state
            }
        });
    }
}