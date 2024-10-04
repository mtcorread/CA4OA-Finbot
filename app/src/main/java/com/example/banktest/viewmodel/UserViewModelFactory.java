package com.example.banktest.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.banktest.database.ExpectedExpenditureDAO;
import com.example.banktest.database.TransactionDAO;
import com.example.banktest.database.UserDAO;
import com.example.banktest.repositories.UserRepository;

import java.util.concurrent.ExecutorService;

public class UserViewModelFactory implements ViewModelProvider.Factory {
    private final UserDAO userDao;
    private final TransactionDAO transactionDao;
    private final ExpectedExpenditureDAO expectedExpenditureDao;
    private final ExecutorService executorService;

    public UserViewModelFactory(UserDAO userDao, TransactionDAO transactionDao, ExpectedExpenditureDAO expectedExpenditureDao, ExecutorService executorService) {
        this.userDao = userDao;
        this.transactionDao = transactionDao;
        this.expectedExpenditureDao = expectedExpenditureDao;
        this.executorService = executorService;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UserViewModel.class)) {
            UserRepository userRepository = new UserRepository(userDao, transactionDao, expectedExpenditureDao, executorService);
            return (T) new UserViewModel(userRepository, executorService);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}


