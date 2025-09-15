// src/main/java/com/hivefi/services/FXService.java  (drop-in replacement)
package com.hivefi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;

public class FXService {
    private final OkHttpClient client;
    private final CacheManager<String, Double> cache;
    private final long ttlMillis;
    private final String apiUrl; // e.g. "https://api.frankfurter.app/latest?from=%s&to=%s"
    private final String apiKey;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public FXService() {
        this(System.getenv("FX_API_URL"),
             System.getenv("FX_API_KEY"),
             readTtlMinutesEnv(30));
    }

    public FXService(String apiUrl, String apiKey, int ttlMinutes) {
        this.client = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(20))
                .build();
        this.cache = new CacheManager<>();
        this.ttlMillis = Duration.ofMinutes(ttlMinutes).toMillis();

        this.apiUrl = (apiUrl == null || apiUrl.isBlank())
                ? "https://api.frankfurter.app/latest?from=%s&to=%s"
                : apiUrl;
        this.apiKey = (apiKey == null || apiKey.isBlank()) ? null : apiKey;
    }

    /** Get FX rate FROM -> TO. Returns 1.0 if codes are identical. Uses a TTL cache. */
    public double getRate(String from, String to) {
        String f = from.toUpperCase(Locale.ROOT);
        String t = to.toUpperCase(Locale.ROOT);
        if (f.equals(t)) return 1.0;

        String key = f + "->" + t;
        Double cached = cache.getIfFresh(key);
        if (cached != null) return cached;

        double fetched = fetchRate(f, t);
        cache.put(key, fetched, ttlMillis);
        return fetched;
    }

    private double fetchRate(String from, String to) {
        String url = formatUrl(from, to);
        Request.Builder rb = new Request.Builder()
                .url(url)
                .header("User-Agent", "HiveFi/1.0 (CLI)");
        if (apiKey != null) rb.header("apikey", apiKey); // ignored by providers that don't use it

        try (Response resp = client.newCall(rb.build()).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) {
                throw new RuntimeException("FX HTTP " + resp.code());
            }
            String body = resp.body().string();
            Double rate = parseRateJson(body, to);
            if (rate == null) {
                String preview = body.replaceAll("\\s+", " ");
                if (preview.length() > 240) preview = preview.substring(0, 240) + "...";
                throw new RuntimeException("FX parse error: " + preview);
            }
            return rate;
        } catch (IOException e) {
            throw new RuntimeException("FX request failed: " + e.getMessage(), e);
        }
    }

    private static Double parseRateJson(String json, String targetCode) {
        try {
            JsonNode root = MAPPER.readTree(json);

            if (root.has("result") && root.get("result").isNumber()) {
                return root.get("result").asDouble();
            }
            if (root.has("info") && root.get("info").has("rate")) {
                JsonNode rate = root.get("info").get("rate");
                if (rate.isNumber()) return rate.asDouble();
            }
            if (root.has("rate") && root.get("rate").isNumber()) {
                return root.get("rate").asDouble();
            }

            // Frankfurter: {"rates":{"USD": 1.07}}
            if (root.has("rates")) {
                JsonNode rates = root.get("rates");
                if (rates.has(targetCode) && rates.get(targetCode).isNumber()) {
                    return rates.get(targetCode).asDouble();
                }
            }

            if (root.has("data")) {
                JsonNode data = root.get("data");
                if (data.has(targetCode) && data.get(targetCode).isNumber()) {
                    return data.get(targetCode).asDouble();
                }
                for (var it = data.fields(); it.hasNext();) {
                    var entry = it.next();
                    JsonNode node = entry.getValue();
                    if (node.has("value") && node.get("value").isNumber()) {
                        return node.get("value").asDouble();
                    }
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String formatUrl(String from, String to) {
        if (apiUrl.contains("%s")) {
            try { return String.format(Locale.ROOT, apiUrl, from, to); }
            catch (Exception ignore) { /* fall through */ }
        }
        String sep = apiUrl.contains("?") ? "&" : "?";
        return apiUrl + sep + "from=" + from + "&to=" + to + "&amount=1";
    }

    private static int readTtlMinutesEnv(int def) {
        String raw = System.getenv("FX_TTL_MINUTES");
        if (raw == null || raw.isBlank()) return def;
        try { return Math.max(1, Integer.parseInt(raw.trim())); }
        catch (NumberFormatException e) { return def; }
    }
}
