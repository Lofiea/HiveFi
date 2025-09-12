// src/com/hivefi/service/ApiRateService.java
package com.hivefi.service;

import com.hivefi.model.Rate;
import com.hivefi.utils.Cache;
import com.hivefi.utils.HttpClientUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ApiRateService implements RateService {
    private static final boolean USE_LIVE_API = false; // flip to true when ready
    private static final String PRIMARY_URL =
            "https://open.er-api.com/v6/latest/USD"; // returns JSON: { "rates": { "EUR": 0.92, ... } }

    // cache keys
    private static final String CACHE_KEY_RATE = "rates:USD";
    // TTL in ms (e.g., 12 hours)
    private static final long TTL_MS = 12 * 60 * 60 * 1000L;

    private final Cache cache = new Cache();

    @Override
    public Optional<Rate> fetchLatestRates(String baseCurrency) {
        // 1) Try cache
        Rate cached = cache.get(CACHE_KEY_RATE);
        if (cached != null && !isStale(cached.fetchedAtMillis())) {
            return Optional.of(cached);
        }

        // 2) Live API or stub
        Rate fresh = USE_LIVE_API ? fetchFromPrimary(baseCurrency) : stub(baseCurrency);

        // 3) Save to cache and return
        if (fresh != null) {
            cache.put(CACHE_KEY_RATE, fresh);
            return Optional.of(fresh);
        }
        return Optional.empty();
    }

    private boolean isStale(long fetchedAt) {
        return (System.currentTimeMillis() - fetchedAt) > TTL_MS;
    }

    private Rate fetchFromPrimary(String base) {
        try {
            String body = HttpClientUtil.get(PRIMARY_URL, 5000);
            Map<String, Double> quotes = new HashMap<>();

            // Extract a few common currencies; expand as needed
            quotes.put("USD", 1.0);
            quotes.put("EUR", extractRate(body, "\"EUR\":"));
            quotes.put("GBP", extractRate(body, "\"GBP\":"));
            quotes.put("JPY", extractRate(body, "\"JPY\":"));
            quotes.put("CAD", extractRate(body, "\"CAD\":"));
            quotes.put("AUD", extractRate(body, "\"AUD\":"));

            if (quotes.get("EUR") == null || quotes.get("EUR") == 0.0) return null;
            return new Rate(base, System.currentTimeMillis(), quotes);
        } catch (Exception e) {
            return null;
        }
    }

    /** Crude parser: get the number immediately following a token like `"EUR":`. */
    private Double extractRate(String json, String token) {
        int i = json.indexOf(token);
        if (i < 0) return null;
        i += token.length();
        // skip spaces
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        // read number until non-number char
        int j = i;
        while (j < json.length()) {
            char c = json.charAt(j);
            if ((c >= '0' && c <= '9') || c == '.' || c == 'e' || c == 'E' || c == '-') {
                j++;
            } else break;
        }
        try {
            return Double.parseDouble(json.substring(i, j));
        } catch (Exception ex) {
            return null;
        }
    }

    private Rate stub(String base) {
        Map<String, Double> quotes = Map.of(
                "USD", 1.0,
                "EUR", 0.920,
                "GBP", 0.790,
                "JPY", 146.000,
                "CAD", 1.360,
                "AUD", 1.520
        );
        return new Rate(base, System.currentTimeMillis(), quotes);
    }
}
