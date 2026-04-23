package com.banking.model;

public class CurrentAccount extends Account {
    private static final double OVERDRAFT_LIMIT = 1000.0;

    public CurrentAccount(String accountNumber, User owner, double initialBalance) {
        super(accountNumber, owner, initialBalance);
    }

    @Override
    protected boolean canWithdraw(double amount) {
        return (balance + OVERDRAFT_LIMIT) >= amount;
    }

    @Override
    public String getAccountType() {
        return "Current";
    }
}
