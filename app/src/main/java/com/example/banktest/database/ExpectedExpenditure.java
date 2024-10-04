package com.example.banktest.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expected_expenditure")
public class ExpectedExpenditure {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String recipient;
    private double amount;
    private String type;
    private long time;

    public ExpectedExpenditure(String recipient, double amount, String type, long time) {
        this.recipient = recipient;
        this.amount = amount;
        this.type = type;
        this.time = time;
    }

    public ExpectedExpenditure() {

    }

    // Getter and setter methods for each field
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
