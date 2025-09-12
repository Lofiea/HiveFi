package com.hivefi.model;

import java.time.Instant;

/**
 * Immutable expense line item.
 * amountNative: value in the transaction's currency (e.g., 25.50 EUR).
 * currency: 3-letter code (e.g., "EUR").
 */
public record Expense(
        String id,
        String tripId,
        String category,        
        double amountNative,
        String currency,
        Instant timestamp
) { }
