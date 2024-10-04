package com.example.banktest.database;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class}, version = 1, exportSchema = false)

public abstract class UserDatabase extends RoomDatabase{

    public abstract UserDAO userDao();

    private static volatile UserDatabase INSTANCE;

    public static UserDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (UserDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    UserDatabase.class, "UserDatabase")
                            .fallbackToDestructiveMigration() // Handle migrations
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
