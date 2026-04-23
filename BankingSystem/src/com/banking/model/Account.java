package com.banking.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Account {
    private String accountNumber;
    private User owner;
    protected double balance;
    private List<Transaction> transactionHistory;

    public Account(String accountNumber, User owner, double initialBalance) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = initialBalance;
        this.transactionHistory = new ArrayList<>();
        if (initialBalance > 0) {
            addTransaction(new Transaction("Deposit", initialBalance, "Initial Deposit"));
        }
    }

    public String getAccountNumber() { return accountNumber; }
    public User getOwner() { return owner; }
    public double getBalance() { return balance; }
    public List<Transaction> getTransactionHistory() { return transactionHistory; }

    protected void addTransaction(Transaction transaction) {
        transactionHistory.add(transaction);
    }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount;
        addTransaction(new Transaction("Deposit", amount, "Self Deposit"));
    }

    public void withdraw(double amount) throws Exception {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        
        // make sure they have enough money
        if (!canWithdraw(amount)) {
            throw new Exception("Insufficient funds.");
        }
        
        balance -= amount;
        addTransaction(new Transaction("Withdrawal", amount, "Self Withdrawal"));
    }

    protected abstract boolean canWithdraw(double amount);

    public void transfer(Account targetAccount, double amount) throws Exception {
        if (amount <= 0) throw new IllegalArgumentException("Transfer amount must be positive.");
        if (!canWithdraw(amount)) {
            throw new Exception("Insufficient funds for transfer.");
        }
        this.balance -= amount;
        this.addTransaction(new Transaction("Transfer Out", amount, "To " + targetAccount.getAccountNumber()));
        
        targetAccount.balance += amount;
        targetAccount.addTransaction(new Transaction("Transfer In", amount, "From " + this.accountNumber));
    }
    
    public abstract String getAccountType();

    public String toJson() {
        return String.format("{\"accountNumber\":\"%s\",\"type\":\"%s\",\"balance\":%.2f}", 
            accountNumber, getAccountType(), balance);
    }
}
