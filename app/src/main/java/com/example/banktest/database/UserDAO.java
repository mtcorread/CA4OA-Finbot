package com.example.banktest.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@Dao
public interface UserDAO {

        @Update
        void updateUser(User user);

        @Insert
        void insert(User user);

        @Query("SELECT * FROM users")
        List<User> getAllUsers();

        @Query("DELETE FROM users")
        void deleteAllUsers();

        @Query("DELETE FROM sqlite_sequence WHERE name = 'users'")
        void resetUserIds();

        @Query("SELECT * FROM users WHERE id = :userId")
        User findUserById(int userId);
}
