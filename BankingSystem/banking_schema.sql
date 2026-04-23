CREATE DATABASE IF NOT EXISTS finsafe_db;
USE finsafe_db;

CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    username VARCHAR(50),
    type VARCHAR(20),
    balance DECIMAL(15, 2),
    FOREIGN KEY (username) REFERENCES users(username)
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20),
    type VARCHAR(20),
    amount DECIMAL(15, 2),
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    details VARCHAR(255),
    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);

-- Default admin user for testing
INSERT IGNORE INTO users (username, password) VALUES ('admin', 'admin123');
INSERT IGNORE INTO accounts (account_number, username, type, balance) VALUES ('ACC10001', 'admin', 'Savings', 5000.00);
