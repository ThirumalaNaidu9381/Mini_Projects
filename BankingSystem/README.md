# FinSafe Banking System

FinSafe is a Java-based banking simulation that demonstrates core banking operations, user account management, and financial transactions. It is built strictly using **Core Java** (OOP principles, Collections Framework) and features a modern, premium **Web UI** served by a custom-built Java HTTP Server.

## Key Features

- **User Authentication:** Secure registration and login.
- **Account Management:** Create Savings or Current accounts.
  - Savings accounts have a minimum balance requirement ($500).
  - Current accounts have an overdraft limit ($1,000).
- **Transactions:** Deposit, withdraw, and transfer money between accounts.
- **Transaction History:** View a detailed history of all transactions (deposits, withdrawals, transfers) with timestamps.
- **Modern UI:** A single-page application built with HTML, CSS, and JS.

## Technology Stack

- **Backend:** Core Java 11+ (`com.sun.net.httpserver.HttpServer` for routing) and JDBC.
- **Data Storage:** MySQL Database (`finsafe_db`).
- **Frontend:** HTML5, CSS3, JavaScript.

## Setup & Execution

1. Make sure you have **Java (JDK 11 or higher)** installed on your system.
2. Clone or download this repository.
3. Ensure **MySQL** is running on `localhost:3306` with user `root` and password (update credentials in `src/com/banking/service/BankingService.java`). The database schema will be initialized automatically, or you can manually import `banking_schema.sql`.
4. Run the `run.bat` script (Windows) to compile and start the server.
   - Alternatively, compile manually: `javac -cp "lib/*" -d bin src/com/banking/**/*.java` and run `java -cp "bin;lib/*" com.banking.Main`.
5. Open your web browser and navigate to `http://localhost:8080/`.

## Built-in Demo Data

To quickly test the application, you can use the built-in admin account:
- **Username:** `admin`
- **Password:** `admin123`

## Video Demo

A video demonstrating the workflow (login, creating accounts, depositing, and transferring funds).

<video controls src="Banking System Simulation.mp4" title="Title"></video>

## Objective Alignment

This project satisfies the required objectives:
- OOP concepts (inheritance with `Account`, `SavingsAccount`, `CurrentAccount`).
- Encapsulation (private fields, getters/setters, controlled access to balance).
- Custom HTTP server and Web UI matches the "make one slightly advanced (with UI or deployment)" requirement.
