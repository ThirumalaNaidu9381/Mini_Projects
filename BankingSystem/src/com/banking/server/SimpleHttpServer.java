package com.banking.server;

import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.model.User;
import com.banking.service.BankingService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SimpleHttpServer {
    private HttpServer server;
    private BankingService bankingService;
    private static final int PORT = 8080;

    public SimpleHttpServer(BankingService service) throws IOException {
        this.bankingService = service;
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/accounts", new AccountsHandler());
        server.createContext("/api/transactions", new TransactionsHandler());
        
        server.setExecutor(null);
    }

    public void start() {
        server.start();
        System.out.println("FinSafe Web Server started on port " + PORT);
        System.out.println("Server is up! Access it at http://localhost:" + PORT + "/");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    // quick and dirty json parser to avoid adding jackson or gson dependencies
    private String extractJsonField(String json, String field) {
        String search = "\"" + field + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) 
            return null; // field not found
        
        start += search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private double extractJsonDouble(String json, String field) {
        String search = "\"" + field + "\":";
        int start = json.indexOf(search);
        if (start == -1) 
            return 0;
        start += search.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        try {
            return Double.parseDouble(json.substring(start, end).trim());
        } 
        catch (Exception e) {
            return 0;
        }
    }

    class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) 
                path = "/index.html";
            
            Path filePath = Paths.get("public" + path);
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                if (path.endsWith(".html")) 
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                else if (path.endsWith(".css")) 
                    exchange.getResponseHeaders().set("Content-Type", "text/css");
                else if (path.endsWith(".js")) 
                    exchange.getResponseHeaders().set("Content-Type", "application/javascript");
                
                byte[] content = Files.readAllBytes(filePath);
                exchange.sendResponseHeaders(200, content.length);
                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
            } 
            else {
                String response = "404 (Not Found)\n";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                String user = extractJsonField(body, "username");
                String pass = extractJsonField(body, "password");
                
                User authenticated = bankingService.authenticate(user, pass);
                if (authenticated != null) {
                    sendResponse(exchange, 200, "{\"status\":\"success\", \"username\":\"" + user + "\"}");
                } 
                else {
                    sendResponse(exchange, 401, "{\"status\":\"error\", \"message\":\"Invalid credentials\"}");
                }
            } 
            else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
                sendResponse(exchange, 204, "");
            }
        }
    }

    class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                String user = extractJsonField(body, "username");
                String pass = extractJsonField(body, "password");
                
                if (bankingService.registerUser(user, pass)) {
                    sendResponse(exchange, 200, "{\"status\":\"success\"}");
                } 
                else {
                    sendResponse(exchange, 400, "{\"status\":\"error\", \"message\":\"Username already exists\"}");
                }
            }
        }
    }

    class AccountsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                String query = exchange.getRequestURI().getQuery(); // e.g., username=admin
                if (query != null && query.startsWith("username=")) {
                    String username = query.substring(9);
                    List<Account> accounts = bankingService.getUserAccounts(username);
                    
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < accounts.size(); i++) {
                        json.append(accounts.get(i).toJson());
                        if (i < accounts.size() - 1) json.append(",");
                    }
                    json.append("]");
                    sendResponse(exchange, 200, json.toString());
                } 
                else {
                    sendResponse(exchange, 400, "{\"error\":\"Missing username\"}");
                }
            } 
            else if ("POST".equals(method)) {
                String body = readRequestBody(exchange);
                String username = extractJsonField(body, "username");
                String type = extractJsonField(body, "type");
                double initialBalance = extractJsonDouble(body, "initialBalance");
                
                try {
                    Account acc = bankingService.createAccount(username, type, initialBalance);
                    if (acc != null) {
                        sendResponse(exchange, 200, "{\"status\":\"success\"}");
                    } 
                    else {
                        sendResponse(exchange, 400, "{\"status\":\"error\", \"message\":\"User not found\"}");
                    }
                } 
                catch (Exception e) {
                    // print stack trace for debugging just in case
                    e.printStackTrace();
                    sendResponse(exchange, 400, "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
                }
            }
        }
    }

    class TransactionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                String query = exchange.getRequestURI().getQuery(); // accountNumber=ACC10001
                if (query != null && query.startsWith("accountNumber=")) {
                    String accNum = query.substring(14);
                    Account acc = bankingService.getAccount(accNum);
                    if (acc != null) {
                        List<Transaction> txs = acc.getTransactionHistory();
                        StringBuilder json = new StringBuilder("[");
                        for (int i = 0; i < txs.size(); i++) {
                            json.append(txs.get(i).toJson());
                            if (i < txs.size() - 1) 
                                json.append(",");
                        }
                        json.append("]");
                        sendResponse(exchange, 200, json.toString());
                    } 
                    else {
                        sendResponse(exchange, 404, "{\"error\":\"Account not found\"}");
                    }
                }
            } 
            else if ("POST".equals(method)) {
                String body = readRequestBody(exchange);
                String action = extractJsonField(body, "action"); // deposit, withdraw, transfer
                String accountNum = extractJsonField(body, "accountNumber");
                double amount = extractJsonDouble(body, "amount");
                
                Account acc = bankingService.getAccount(accountNum);
                if (acc == null) {
                    sendResponse(exchange, 404, "{\"status\":\"error\", \"message\":\"Account not found\"}");
                    return;
                }

                try {
                    if ("deposit".equals(action)) {
                        bankingService.performDeposit(accountNum, amount);
                    } 
                    else if ("withdraw".equals(action)) {
                        bankingService.performWithdraw(accountNum, amount);
                    } 
                    else if ("transfer".equals(action)) {
                        String targetAccNum = extractJsonField(body, "targetAccount");
                        bankingService.performTransfer(accountNum, targetAccNum, amount);
                    } 
                    else {
                        throw new Exception("Invalid action");
                    }
                    sendResponse(exchange, 200, "{\"status\":\"success\"}");
                } 
                catch (Exception e) {
                    // print stack trace for debugging just in case
                    e.printStackTrace();
                    sendResponse(exchange, 400, "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
                }
            }
        }
    }
}
