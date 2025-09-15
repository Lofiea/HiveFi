package com.hivefi.models;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

public class Transaction {

    public enum Action { CREATE, UPDATE, DELETE }

    private final String id;          
    private final Action action;       
    private final String expenseId;    
    private final String category;  
    private final String currency;    
    private final double amount;       
    private final String date;         
    private final String description;  
    private final String timestamp;   
    private final String prevHash;     
    private final String txHash;       

    public Transaction(Action action, Expense expense, String prevHash) {
        this.id          = UUID.randomUUID().toString();
        this.action      = action;
        this.expenseId   = expense.getID();
        this.category    = expense.getCategory();
        this.currency    = expense.getCurrency();
        this.amount      = expense.getAmount();
        this.date        = expense.getDate();
        this.description = expense.getDescription();
        this.timestamp   = Instant.now().toString();
        this.prevHash    = prevHash == null ? "" : prevHash;
        this.txHash      = computeHash();
    }

    // Getters
    public String getId() { return id; }
    public Action getAction() { return action; }
    public String getExpenseId() { return expenseId; }
    public String getCategory() { return category; }
    public String getCurrency() { return currency; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
    public String getTimestamp() { return timestamp; }
    public String getPrevHash() { return prevHash; }
    public String getTxHash() { return txHash; }

    // --- Hashing ---

    private String computeHash() {
        String payload =
                "id=" + id +
                "|ts=" + timestamp +
                "|action=" + action +
                "|expenseId=" + expenseId +
                "|category=" + nullSafe(category) +
                "|currency=" + nullSafe(currency) +
                "|amount=" + amount +
                "|date=" + nullSafe(date) +
                "|desc=" + nullSafe(description) +
                "|prev=" + nullSafe(prevHash);
        return sha256Hex(payload);
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }

    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @Override
    public String toString() {
        return String.format(
            "{\n" +
            "  \"id\": \"%s\",\n" +
            "  \"action\": \"%s\",\n" +
            "  \"expenseId\": \"%s\",\n" +
            "  \"snapshot\": {\n" +
            "    \"category\": \"%s\",\n" +
            "    \"amount\": \"%.2f\",\n" +
            "    \"currency\": \"%s\",\n" +
            "    \"description\": \"%s\",\n" +
            "    \"date\": \"%s\"\n" +
            "  },\n" +
            "  \"timestamp\": \"%s\",\n" +
            "  \"prevHash\": \"%s\",\n" +
            "  \"txHash\": \"%s\"\n" +
            "}",
            id,
            action,
            expenseId,
            category,
            amount,
            currency,
            description == null ? "" : description,
            date,
            timestamp,
            prevHash,
            txHash
        );
    }
}
