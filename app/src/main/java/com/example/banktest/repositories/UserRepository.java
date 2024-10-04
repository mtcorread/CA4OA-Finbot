package com.example.banktest.repositories;

import com.example.banktest.database.ExpectedExpenditure;
import com.example.banktest.database.ExpectedExpenditureDAO;
import com.example.banktest.database.ExpenditureDatabase;
import com.example.banktest.database.TransactionDAO;
import com.example.banktest.database.Transactions;
import com.example.banktest.database.TransactionsDatabase;
import com.example.banktest.database.User;
import com.example.banktest.database.UserDAO;

import java.util.List;
import java.util.concurrent.ExecutorService;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class UserRepository {
    private final UserDAO userDao;
    private final TransactionDAO transactionDao;
    private final ExpectedExpenditureDAO expectedExpenditureDao;
    private final ExecutorService executorService;

    public UserRepository(UserDAO userDao, TransactionDAO transactionDao, ExpectedExpenditureDAO expectedExpenditureDao, ExecutorService executorService) {
        this.userDao = userDao;
        this.transactionDao = transactionDao;
        this.expectedExpenditureDao = expectedExpenditureDao;
        this.executorService = executorService;
    }

    public User getUserById(int userId) {
        return userDao.findUserById(userId);
    }

    public List<Transactions> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }

    public List<ExpectedExpenditure> getAllExpectedExpenditures() {
        return expectedExpenditureDao.getAllExpectedExpenditures();
    }

    public double getTotalExpenditure() {
        return expectedExpenditureDao.getTotalExpenditure();
    }

    public void updateUser(User user) {
        executorService.execute(() -> userDao.updateUser(user));
    }
}


