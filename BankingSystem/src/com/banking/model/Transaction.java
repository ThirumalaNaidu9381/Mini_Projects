package com.banking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private String type; // Deposit, Withdrawal, Transfer
    private double amount;
    private LocalDateTime timestamp;
    private String details;

    public Transaction(String type, double amount, String details) {
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getTimestampStr() { 
        // formatting the date nicely for the frontend
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }
    public String getDetails() { return details; }

    public String toJson() {
        return String.format("{\"type\":\"%s\",\"amount\":%.2f,\"timestamp\":\"%s\",\"details\":\"%s\"}", 
            type, amount, getTimestampStr(), details);
    }
}
