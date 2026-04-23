package com.banking.model;

public class SavingsAccount extends Account {
    private static final double MINIMUM_BALANCE = 500.0;

    public SavingsAccount(String accountNumber, User owner, double initialBalance) {
        super(accountNumber, owner, initialBalance);
    }

    @Override
    protected boolean canWithdraw(double amount) {
        return (balance - amount) >= MINIMUM_BALANCE;
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }
}
