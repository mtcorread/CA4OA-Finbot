package com.example.banktest.repositories;

import com.example.banktest.database.TransactionDAO;
import com.example.banktest.database.Transactions;

import java.util.concurrent.ExecutorService;

public class TransactionRepository {
    private TransactionDAO transactionDao;
    private ExecutorService executorService;

    public TransactionRepository(TransactionDAO transactionDao, ExecutorService executorService) {
        this.transactionDao = transactionDao;
        this.executorService = executorService;
    }

    public void insertTransaction(Transactions transaction) {
        executorService.execute(() -> transactionDao.insert(transaction));
    }
}

