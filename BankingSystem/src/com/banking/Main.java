package com.banking;

import com.banking.server.SimpleHttpServer;
import com.banking.service.BankingService;

public class Main {
    public static void main(String[] args) {
        try {
            BankingService bankingService = new BankingService();
            SimpleHttpServer server = new SimpleHttpServer(bankingService);
            
            // let's go!
            server.start();
        } 
        catch (Exception e) {
            System.err.println("Failed to start the server :(");
            e.printStackTrace();
        }
    }
}
