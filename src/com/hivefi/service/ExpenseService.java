package com.hivefi.service;

import com.hivefi.model.Expense;

import java.util.*;
import java.util.stream.Collectors;


public class ExpenseService {

    private final List<Expense> store = new ArrayList<>();
    private final Converter converter;

    public ExpenseService(Converter converter) {
        this.converter = converter;
    }

    /** Create */
    public void add(Expense e) { store.add(e); }

    /** Read */
    public List<Expense> listByTrip(String tripId) {
        return store.stream().filter(e -> Objects.equals(e.tripId(), tripId)).collect(Collectors.toList());
    }

    /** Delete by id */
    public boolean remove(String id) {
        return store.removeIf(e -> Objects.equals(e.id(), id));
    }

    /** Clears a whole trip */
    public void clearTrip(String tripId) {
        store.removeIf(e -> Objects.equals(e.tripId(), tripId));
    }

    public double totalTripInHome(String tripId, String homeCurrency) {
        double sum = 0.0;
        for (Expense e : listByTrip(tripId)) {
            double converted = converter.convert(e.amountNative(), e.currency(), homeCurrency);
            if (!Double.isNaN(converted)) sum += converted;
        }
        return Converter.round(sum, 2); // totals as 2-decimal money representation
    }

    /** Simple per-category breakdown in home currency. */
    public Map<String, Double> categoryTotalsInHome(String tripId, String homeCurrency) {
        Map<String, Double> out = new LinkedHashMap<>();
        for (Expense e : listByTrip(tripId)) {
            double converted = converter.convert(e.amountNative(), e.currency(), homeCurrency);
            if (Double.isNaN(converted)) continue;
            out.merge(e.category(), converted, Double::sum);
        }
        // round to 2 dp for each category
        out.replaceAll((_,v) -> Converter.round(v, 2));
        return out;
        }
}
