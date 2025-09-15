package com.hivefi.models;

import java.util.Locale;

public enum Category {
    FOOD("Food"),
    LODGING("Lodging"),
    TRANSPORT("Transport"),
    ENTERTAINMENT("Entertainment"),
    GROCERIES("Groceries"),
    TUITION("Tuition"),
    RENT("Rent"),
    UTILITIES("Utilities"),
    HEALTH("Health"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String display() {
        return displayName;
    }

    public String code() {
        return name();
    }

    public static Category from(String input) {
        if (input == null) return OTHER;
        String s = input.trim().toLowerCase(Locale.ROOT);
        switch (s) {
            // Food
            case "food": case "meal": case "meals": case "dining": case "restaurant":
                return FOOD;
            // Lodging / stays
            case "lodging": case "hotel": case "hostel": case "accommodation": case "stay":
                return LODGING;
            // Transport
            case "transport": case "transportation": case "uber": case "taxi":
            case "bus": case "train": case "metro": case "subway": case "flight":
            case "flights": case "airfare": case "ride":
                return TRANSPORT;
            // Entertainment
            case "entertainment": case "movie": case "movies": case "cinema":
            case "music": case "game": case "games": case "concert":
                return ENTERTAINMENT;
            // Groceries
            case "groceries": case "grocery": case "supermarket":
                return GROCERIES;
            // Tuition / education
            case "tuition": case "school": case "fees": case "education":
                return TUITION;
            // Rent / housing
            case "rent": case "housing": case "apartment": case "dorm":
                return RENT;
            // Utilities
            case "utilities": case "utility": case "electric": case "electricity":
            case "water": case "internet": case "wifi": case "gas":
                return UTILITIES;
            // Health
            case "health": case "medical": case "pharmacy": case "medicine": case "doctor":
                return HEALTH;
            default:
                return OTHER;
        }
    }

    public static String normalize(String raw) {
        return from(raw).display();
    }

    @Override
    public String toString() {
        return displayName;
    }
}
