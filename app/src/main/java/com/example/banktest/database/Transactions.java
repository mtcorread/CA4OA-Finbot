package com.example.banktest.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Calendar;

import java.util.Calendar;

@Entity(tableName = "transactions")
public class Transactions {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String recipientName;
    public String bankAccount;
    public String sortCode;
    public String amount; // Store as String to avoid floating-point inaccuracies with currency, or consider BigDecimal for arithmetic operations

    public int day;
    public int month;
    public int year;
    public int hour;
    public int minute;
    public long timestamp; // Store the original UNIX timestamp for ordering


    // Constructor, getters, and setters as needed

    public Transactions() {}

    public Transactions(String name, String BA, String SC, String amount, long timestamp) {
        this.recipientName = name;
        this.bankAccount = BA;
        this.sortCode = SC;
        this.amount = amount;
        this.timestamp = timestamp;
        setDateFromTimestamp(timestamp);
    }

    private void setDateFromTimestamp(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        this.day = calendar.get(Calendar.DAY_OF_MONTH);
        this.month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is zero-based
        this.year = calendar.get(Calendar.YEAR);
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getSortCode() {
        return sortCode;
    }

    public void setSortCode(String sortCode) {
        this.sortCode = sortCode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public long getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }
}



