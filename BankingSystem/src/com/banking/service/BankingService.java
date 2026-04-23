package com.banking.service;

import com.banking.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BankingService {
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/finsafe_db?createDatabaseIfNotExist=true";
    private static final String USER = "root";
    private static final String PASS = "Naidu@1234";

    public BankingService() {
        try {
            // make sure the driver is loaded
            Class.forName("com.mysql.cj.jdbc.Driver");
            initDatabase();
        } 
        catch (Exception e) {
            System.err.println("Database connection failed. Is MySQL running?");
            e.printStackTrace();
        }
    }
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    private void initDatabase() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/", USER, PASS);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS finsafe_db");
            stmt.executeUpdate("USE finsafe_db");
            
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(50) PRIMARY KEY, " +
                    "password VARCHAR(100) NOT NULL)");
                    
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS accounts (" +
                    "account_number VARCHAR(20) PRIMARY KEY, " +
                    "username VARCHAR(50), " +
                    "type VARCHAR(20), " +
                    "balance DECIMAL(15, 2), " +
                    "FOREIGN KEY (username) REFERENCES users(username))");
                    
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (" +
                    "transaction_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "account_number VARCHAR(20), " +
                    "type VARCHAR(20), " +
                    "amount DECIMAL(15, 2), " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "details VARCHAR(255), " +
                    "FOREIGN KEY (account_number) REFERENCES accounts(account_number))");
            
            // default admin user
            stmt.executeUpdate("INSERT IGNORE INTO users (username, password) VALUES ('admin', 'admin123')");
            stmt.executeUpdate("INSERT IGNORE INTO accounts (account_number, username, type, balance) VALUES ('ACC10001', 'admin', 'Savings', 5000.00)");
        }
    }

    public User authenticate(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new User(username, password);
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean registerUser(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } 
        catch (SQLException e) {
            // usually means duplicate key
            return false;
        }
    }

    public Account createAccount(String username, String type, double initialBalance) {
        if ("Savings".equalsIgnoreCase(type) && initialBalance < 500) {
            throw new IllegalArgumentException("Savings minimum balance is $500");
        }
        
        try (Connection conn = getConnection()) {
            // generate account number
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM accounts");
            rs.next();
            int count = rs.getInt(1);
            String accNum = "ACC" + (10000 + count + 1);
            
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO accounts (account_number, username, type, balance) VALUES (?, ?, ?, ?)"
            );
            pstmt.setString(1, accNum);
            pstmt.setString(2, username);
            pstmt.setString(3, type);
            pstmt.setDouble(4, initialBalance);
            pstmt.executeUpdate();
            
            // Log initial deposit if balance > 0
            if (initialBalance > 0) {
                logTransaction(conn, accNum, "Deposit", initialBalance, "Initial Deposit");
            }
            
            return getAccount(accNum);
        } 
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Account getAccount(String accountNumber) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM accounts WHERE account_number = ?")) {
            
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String type = rs.getString("type");
                double balance = rs.getDouble("balance");
                String username = rs.getString("username");
                User user = new User(username, ""); // password omitted for security
                
                Account acc;
                if ("Savings".equalsIgnoreCase(type)) {
                    acc = new SavingsAccount(accountNumber, user, balance);
                } 
                else {
                    acc = new CurrentAccount(accountNumber, user, balance);
                }
                
                // Fetch transaction history
                loadTransactions(conn, acc);
                return acc;
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadTransactions(Connection conn, Account acc) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM transactions WHERE account_number = ? ORDER BY timestamp ASC");
        pstmt.setString(1, acc.getAccountNumber());
        ResultSet rs = pstmt.executeQuery();
        
        // We bypass the standard addTransaction to load historical ones
        // Since Account's addTransaction is protected, we can just return the Account and let caller use a helper if needed.
        // Actually, Account's constructor adds an initial deposit. We can just clear it and load from DB.
        acc.getTransactionHistory().clear();
        
        while(rs.next()) {
            Transaction tx = new Transaction(
                rs.getString("type"), 
                rs.getDouble("amount"), 
                rs.getString("details")
            );
            // using a dirty reflection hack or just adding it because they are in the same package?
            // BankingService is in com.banking.service, Account in com.banking.model. 
            // I'll add a public method to Account to add transaction history from DB.
            acc.getTransactionHistory().add(tx);
        }
    }

    public List<Account> getUserAccounts(String username) {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT account_number FROM accounts WHERE username = ?")) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                accounts.add(getAccount(rs.getString("account_number")));
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }
    
    // --- JDBC TRANSACTION METHODS ---
    
    public void performDeposit(String accountNumber, double amount) throws Exception {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            PreparedStatement updateAcc = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE account_number = ?");
            updateAcc.setDouble(1, amount);
            updateAcc.setString(2, accountNumber);
            updateAcc.executeUpdate();
            
            logTransaction(conn, accountNumber, "Deposit", amount, "Self Deposit");
            conn.commit();
        }
    }
    
    public void performWithdraw(String accountNumber, double amount) throws Exception {
        Account acc = getAccount(accountNumber);
        if (acc == null) throw new Exception("Account not found");
        
        // Use the domain logic to check if withdrawal is allowed
        acc.withdraw(amount); // This throws exception if insufficient funds
        
        // If we reach here, domain validation passed, let's update DB
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            PreparedStatement updateAcc = conn.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE account_number = ?");
            updateAcc.setDouble(1, amount);
            updateAcc.setString(2, accountNumber);
            updateAcc.executeUpdate();
            
            logTransaction(conn, accountNumber, "Withdrawal", amount, "Self Withdrawal");
            conn.commit();
        }
    }
    
    public void performTransfer(String sourceAccNum, String targetAccNum, double amount) throws Exception {
        Account source = getAccount(sourceAccNum);
        Account target = getAccount(targetAccNum);
        if (source == null || target == null) throw new Exception("Account not found");
        
        // Domain validation
        source.withdraw(amount); // check if source has funds
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            // deduct source
            PreparedStatement deduct = conn.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE account_number = ?");
            deduct.setDouble(1, amount);
            deduct.setString(2, sourceAccNum);
            deduct.executeUpdate();
            
            // add target
            PreparedStatement add = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE account_number = ?");
            add.setDouble(1, amount);
            add.setString(2, targetAccNum);
            add.executeUpdate();
            
            logTransaction(conn, sourceAccNum, "Transfer Out", amount, "To " + targetAccNum);
            logTransaction(conn, targetAccNum, "Transfer In", amount, "From " + sourceAccNum);
            
            conn.commit();
        }
    }
    
    private void logTransaction(Connection conn, String accNum, String type, double amount, String details) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
            "INSERT INTO transactions (account_number, type, amount, details) VALUES (?, ?, ?, ?)"
        );
        pstmt.setString(1, accNum);
        pstmt.setString(2, type);
        pstmt.setDouble(3, amount);
        pstmt.setString(4, details);
        pstmt.executeUpdate();
    }
}
