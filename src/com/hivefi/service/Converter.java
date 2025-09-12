package com.hivefi.service;

import com.hivefi.model.Rate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class Converter {

    private final RateService rateService;

    public Converter(RateService rateService) {
        this.rateService = rateService;
    }

    /**
     * Convert an amount from one currency to another using latest known rates.
     *
     * @param amount amount in "from" currency
     * @param from   3-letter code, e.g., "USD"
     * @param to     3-letter code, e.g., "EUR"
     * @return converted amount with 3-decimal precision; NaN if unavailable
     */
    public double convert(double amount, String from, String to) {
        if (from == null || to == null) return Double.NaN;
        if (from.equalsIgnoreCase(to)) return round(amount, 3);

        Optional<Rate> rateOpt = rateService.fetchLatestRates("USD");
        if (rateOpt.isEmpty()) return Double.NaN;

        var quotes = rateOpt.get().quotes();
        Double rFrom = quotes.get(from.toUpperCase());
        Double rTo   = quotes.get(to.toUpperCase());
        if (rFrom == null || rTo == null || rFrom == 0.0) return Double.NaN;

        // amount_in_usd = amount / rFrom; amount_out = amount_in_usd * rTo
        double amountInUsd = amount / rFrom;
        double amountOut   = amountInUsd * rTo;
        return round(amountOut, 3);
    }

    /** BigDecimal rounding helper (3-decimal precision by default). */
    public static double round(double value, int scale) {
        return new BigDecimal(value)
                .setScale(scale, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
