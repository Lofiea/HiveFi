package com.hivefi.models;

import java.util.UUID; 

public class Expense {
    
    private String id; 
    private String category; 
    private double amount; 
    private String currency; 
    private String description; 
    private String date; 
    
    public Expense(String category, String currency, double amount, String description, String date) { 
        this.id = UUID.randomUUID().toString(); 
        this.category = category; 
        this.currency = currency;
        this.amount = amount; 
        this.description = description; 
        this.date = date; 
    }

    public Expense(String id, String category, String currency, double amount, String description, String date) { 
        this.id = id; 
        this.category = category;
        this.currency = currency;  
        this.amount = amount; 
        this.description = description; 
        this.date = date; 
    }

    // Getters
    public String getID() { 
        return id; 
    }

    public String getCategory() { 
        return category; 
    }

    public String getCurrency() { 
        return currency; 
    }

    public double getAmount() { 
        return amount; 
    }

    public String getDescription() { 
        return description; 
    }

    public String getDate() { 
        return date; 
    }

    // Setters
    public void setCategory(String category) { 
        this.category = category; 
    }

    public void setCurrency(String currency) { 
        this.currency = currency; 
    }

    public void setAmount(double amount) { 
        this.amount = amount; 
    } 

    public void setDescription(String description) { 
        this.description = description; 
    }

    public void setDate(String date) { 
        this.date = date; 
    }

    private String currencySymbol() { 
        switch (currency) { 
            case "USD": return "$"; 
            case "EUR": return "€"; 
            case "GBP": return "£"; 
            case "JPY": return "¥"; 
            case "CNY": return "¥"; 
            case "INR": return "₹"; 
            case "AUD": return "A$"; 
            case "CAD": return "C$"; 
            case "CHF": return "CHF"; 
            case "KRW": return "₩"; 
            case "RUB": return "₽"; 
            default: return currency; 
        }
    }

    @Override 
    public String toString() {
        return String.format(
            "{\n" +
            "  \"id\": \"%s\",\n" +
            "  \"category\": \"%s\",\n" +
            "  \"amount\": \"%s%.2f\",\n" +         
            "  \"description\": \"%s\",\n" +
            "  \"date\": \"%s\"\n" +
            "}",
            id,
            category,
            currencySymbol(), amount, 
            description,
            date
        );
    }
}
