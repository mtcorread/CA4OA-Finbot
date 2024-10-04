package com.example.banktest.database;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

@Dao
public interface TransactionDAO {
    @Insert
    void insert(Transactions transaction);

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    List<Transactions> getAllTransactions();

    @Query("DELETE FROM transactions")
    void deleteAllTransactions();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'transactions'")
    void resetTransactionIds();

    // Add a RawQuery method that returns a List of Transactions
    @RawQuery
    Cursor executeQuery(SupportSQLiteQuery query);

}
