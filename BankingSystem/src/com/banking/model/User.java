package com.banking.model;

public class User {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { 
        return username; 
    }
    
    // checks if the provided password matches
    public boolean checkPassword(String password) { 
        return this.password.equals(password); 
    }
}
