package com.example.banktest.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
@Database(entities = {Transactions.class}, version = 2  )
public abstract class TransactionsDatabase extends RoomDatabase {
    public abstract TransactionDAO transactionDao();

    // Singleton instance
    private static volatile TransactionsDatabase INSTANCE;

    // Get the singleton instance of the database
    public static TransactionsDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TransactionsDatabase.class) {
                if (INSTANCE == null) {
                    // Create the database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    TransactionsDatabase.class, "TransactionsDatabase")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
