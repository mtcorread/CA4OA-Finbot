package com.example.banktest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private MutableLiveData<Boolean> _navigateToTransfer = new MutableLiveData<>();
    private MutableLiveData<Boolean> _navigateToBalance = new MutableLiveData<>();
    private MutableLiveData<Boolean> _navigateToChat = new MutableLiveData<>();
    private MutableLiveData<Boolean> _navigateToSettings = new MutableLiveData<>();
    private MutableLiveData<Boolean> pensionClickEvent = new MutableLiveData<>();

    public LiveData<Boolean> navigateToTransfer = _navigateToTransfer;
    public LiveData<Boolean> navigateToBalance = _navigateToBalance;
    public LiveData<Boolean> navigateToChat = _navigateToChat;
    public LiveData<Boolean> navigateToSettings = _navigateToSettings;
    public LiveData<Boolean> getPensionClickEvent() {
        return pensionClickEvent;
    }


    public void onTransferClicked() {
        _navigateToTransfer.setValue(true);
    }

    public void onBalanceClicked() {
        _navigateToBalance.setValue(true);
    }

    public void onChatClicked() {
        _navigateToChat.setValue(true);
    }

    public void onSettingsClicked() {
        _navigateToSettings.setValue(true);
    }

    public void onPensionClicked() {
        pensionClickEvent.setValue(true);
    }

    // Reset methods to update the LiveData back to false
    public void doneNavigatingToTransfer() {
        _navigateToTransfer.setValue(false);
    }

    public void doneNavigatingToBalance() {
        _navigateToBalance.setValue(false);
    }

    public void doneNavigatingToChat() {
        _navigateToChat.setValue(false);
    }

    public void doneNavigatingToSettings() {
        _navigateToSettings.setValue(false);
    }

    public void resetPensionClickEvent() {
        pensionClickEvent.setValue(false);
    }
}

