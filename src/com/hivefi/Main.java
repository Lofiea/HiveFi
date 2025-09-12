package com.hivefi;

import com.hivefi.model.Expense;
import com.hivefi.service.*;
import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        //Create core services
        ApiRateService api = new ApiRateService();
        Converter converter = new Converter(api);
        ExpenseService expenses = new ExpenseService(converter);

        //Test simple conversion (stubbed rates first)
        double usdToEur = converter.convert(100, "USD", "EUR");
        System.out.printf("100 USD = %.3f EUR%n", usdToEur);

        double eurToJpy = converter.convert(50, "EUR", "JPY");
        System.out.printf("50 EUR = %.3f JPY%n", eurToJpy);

        //Add sample expenses for a trip
        String tripId = "europe-2025";
        expenses.add(new Expense("1", tripId, "Hotel", 200, "EUR", Instant.now()));
        expenses.add(new Expense("2", tripId, "Food", 70, "EUR", Instant.now()));
        expenses.add(new Expense("3", tripId, "Transport", 8000, "JPY", Instant.now()));
        expenses.add(new Expense("4", tripId, "Shopping", 150, "USD", Instant.now()));

        //Get trip total in USD
        double totalUsd = expenses.totalTripInHome(tripId, "USD");
        System.out.printf("Total trip cost in USD: %.2f%n", totalUsd);

        //Show breakdown per category in USD
        System.out.println("\nCategory breakdown in USD:");
        expenses.categoryTotalsInHome(tripId, "USD")
                .forEach((category, total) ->
                        System.out.printf("  %s: %.2f USD%n", category, total));
    }
}
