import java.util.ArrayList;
import java.util.Scanner;

// Custom Exception for overdrafts as requested
class InSufficientFundsException extends Exception {
    public InSufficientFundsException(String message) {
        super(message);
    }
}

// Account class with encapsulation
class Account {
    private String accountHolder;
    private double balance;
    private ArrayList<Double> transactionHistory;

    public Account(String accountHolder) {
        this.accountHolder = accountHolder;
        this.balance = 0.0;
        this.transactionHistory = new ArrayList<>();
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public double getBalance() {
        return balance;
    }

    // Validation logic for spending money
    public void processTransaction(double amount) throws InSufficientFundsException {
        // If the amount is negative, throw an IllegalArgumentException
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative!");
        }
        // If the amount > balance, throw custom exception
        if (amount > balance) {
            throw new InSufficientFundsException("Overdraft error: You only have $" + balance + " in your account.");
        }

        balance -= amount;
        addTransaction(-amount);
        System.out.println("Transaction processed successfully. Remaining balance: $" + balance);
    }

    // Method to handle deposits
    public void deposit(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("You can't deposit a negative amount!");
        }
        balance += amount;
        addTransaction(amount);
        System.out.println("Deposit successful. New balance: $" + balance);
    }

    // Helper to keep only the last 5 transactions
    private void addTransaction(double amount) {
        // limit history to 5 elements
        if (transactionHistory.size() >= 5) {
            transactionHistory.remove(0);
        }
        transactionHistory.add(amount);
    }

    // Print mini statement
    public void printMiniStatement() {
        System.out.println();
        System.out.println("--- Mini Statement ---");
        System.out.println("User: " + accountHolder);
        System.out.println("Balance: $" + balance);
        System.out.println("History (Last 5):");

        if (transactionHistory.isEmpty()) {
            System.out.println(" -> No transactions yet.");
        } else {
            for (Double amt : transactionHistory) {
                if (amt < 0) {
                    System.out.println(" -> Spend: $" + Math.abs(amt));
                } else {
                    System.out.println(" -> Deposit: $" + amt);
                }
            }
        }
        System.out.println("----------------------");
    }
}

// Main class representing the console application
public class FinSafe {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to FinSafe Digital Wallet");
        System.out.print("Please enter the account holder's name: ");
        String name = scanner.nextLine();

        Account myAccount = new Account(name);
        boolean appRunning = true;

        while (appRunning) {
            System.out.println("\nWhat would you like to do?");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw (Spend)");
            System.out.println("3. View History");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            try {
                if (choice.equals("1")) {
                    System.out.print("Enter deposit amount: ");
                    double depAmt = Double.parseDouble(scanner.nextLine());
                    myAccount.deposit(depAmt);

                } else if (choice.equals("2")) {
                    System.out.print("Enter spend amount: ");
                    double spendAmt = Double.parseDouble(scanner.nextLine());
                    myAccount.processTransaction(spendAmt);

                } else if (choice.equals("3")) {
                    myAccount.printMiniStatement();

                } else if (choice.equals("4")) {
                    System.out.println("Visit Again " + myAccount.getAccountHolder() + "!");
                    appRunning = false;

                } else {
                    System.out.println("Invalid option. Please enter a number between 1 and 4.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Please type a valid numerical amount.");
            } catch (IllegalArgumentException e) {
                System.out.println("Validation Error: " + e.getMessage());
            } catch (InSufficientFundsException e) {
                System.out.println("Transaction Failed: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Oops, something went wrong: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
