package com.example.banktest.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ExpectedExpenditure.class}, version = 2, exportSchema = false)
public abstract class ExpenditureDatabase extends RoomDatabase {
    public abstract ExpectedExpenditureDAO expectedExpenditureDao();

    private static volatile ExpenditureDatabase INSTANCE;

    // Get the singleton instance of the database
    public static ExpenditureDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ExpenditureDatabase.class) {
                if (INSTANCE == null) {
                    // Create the database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ExpenditureDatabase.class, "expenditure_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

