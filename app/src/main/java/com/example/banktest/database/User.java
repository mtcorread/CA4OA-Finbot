package com.example.banktest.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String accountNumber;
    public String balance;

    public User(String name, String accountNumber, String balance) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    private static final Map<String, Function<User, String>> gettersMap = createMap();

    private static Map<String, Function<User, String>> createMap() {
        Map<String, Function<User, String>> map = new HashMap<>();
        map.put("getName", User::getName);
        map.put("getAccountNumber", User::getAccountNumber);
        map.put("getBalance", User::getBalance);
        return map;
    }


    // Getters and setters

    public void subtractFromBalance(BigDecimal amount) {
        BigDecimal currentBalance = new BigDecimal(balance); // Convert balance to integer
        currentBalance = currentBalance.subtract(amount); // Subtract amount
        balance = String.valueOf(currentBalance); // Convert back to String
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    // Method to dynamically invoke any setter
    public static String invokeGetter(User user, String methodName) {
        if (gettersMap.containsKey(methodName)) {
            return gettersMap.get(methodName).apply(user);
        } else {
            return "Method not supported";
        }
    }

}

