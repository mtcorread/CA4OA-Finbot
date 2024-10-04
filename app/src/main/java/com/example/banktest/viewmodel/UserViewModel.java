package com.example.banktest.viewmodel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.banktest.database.ExpectedExpenditure;
import com.example.banktest.database.Transactions;
import com.example.banktest.database.User;
import com.example.banktest.repositories.UserRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import java.util.concurrent.ExecutorService;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class UserViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<List<Transactions>> transactions = new MutableLiveData<>();
    private final MutableLiveData<List<ExpectedExpenditure>> expenses = new MutableLiveData<>();
    private final MutableLiveData<Double> totalExpenditure = new MutableLiveData<>();
    private final ExecutorService executorService;

    public UserViewModel(UserRepository userRepository, ExecutorService executorService) {
        this.userRepository = userRepository;
        this.executorService = executorService;
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<List<Transactions>> getTransactions() {
        return transactions;
    }

    public LiveData<List<ExpectedExpenditure>> getExpenses() {
        return expenses;
    }

    public LiveData<Double> getTotalExpenditure() {
        return totalExpenditure;
    }

    public void loadUser(int userId) {
        executorService.execute(() -> {
            User user = userRepository.getUserById(userId);
            currentUser.postValue(user);
        });
    }

    public void loadTransactions() {
        executorService.execute(() -> {
            List<Transactions> transactionList = userRepository.getAllTransactions();
            transactions.postValue(transactionList);
        });
    }

    public void loadExpenses() {
        executorService.execute(() -> {
            List<ExpectedExpenditure> expensesList = userRepository.getAllExpectedExpenditures();
            double total = userRepository.getTotalExpenditure();
            expenses.postValue(expensesList);
            totalExpenditure.postValue(total);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}




