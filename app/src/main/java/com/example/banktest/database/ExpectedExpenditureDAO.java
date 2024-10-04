package com.example.banktest.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ExpectedExpenditureDAO {
    @Insert
    void insert(ExpectedExpenditure expenditure);

    @Query("SELECT * FROM expected_expenditure")
    List<ExpectedExpenditure> getAllExpectedExpenditures();

    @Query("DELETE FROM expected_expenditure")
    void deleteAllExpenses();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'expected_expenditure'")
    void resetExpensesIds();

    @Query("SELECT SUM(amount) FROM expected_expenditure")
    Double getTotalExpenditure();

    // Add more query methods as needed (e.g., getExpenditureByName, deleteExpenditure, etc.)
}

