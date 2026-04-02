package com.portfolio.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.config.KiteConfig;
import com.portfolio.dto.kite.KiteHolding;
import com.portfolio.dto.kite.KiteSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class KiteClient {

    private static final String KITE_API_BASE = "https://api.kite.trade";
    private static final String KITE_LOGIN_URL = "https://kite.zerodha.com/connect/login";

    private final KiteConfig kiteConfig;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String accessToken;

    public boolean isConfigured() {
        String key = kiteConfig.getApiKey();
        String secret = kiteConfig.getApiSecret();
        return key != null && !key.isBlank() && secret != null && !secret.isBlank();
    }

    public String getLoginUrl() {
        if (!isConfigured()) throw new IllegalStateException("Kite API keys not configured");
        return KITE_LOGIN_URL + "?api_key=" + kiteConfig.getApiKey() + "&v=3";
    }

    public String generateSession(String requestToken) throws Exception {
        if (!isConfigured()) throw new IllegalStateException("Kite API keys not configured");
        String checksum = sha256(kiteConfig.getApiKey() + requestToken + kiteConfig.getApiSecret());

        String formBody = "api_key=" + encode(kiteConfig.getApiKey())
                + "&request_token=" + encode(requestToken)
                + "&checksum=" + encode(checksum);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(KITE_API_BASE + "/session/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-Kite-Version", "3")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = objectMapper.readTree(response.body());

        if (!"success".equals(root.path("status").asText())) {
            throw new RuntimeException("Kite session error: " + root.path("message").asText());
        }

        KiteSession session = objectMapper.treeToValue(root.path("data"), KiteSession.class);
        this.accessToken = session.getAccessToken();
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Map<String, BigDecimal> getLivePrices(Set<String> symbols) {
        Map<String, BigDecimal> prices = new HashMap<>();
        if (accessToken == null || symbols.isEmpty()) return prices;

        try {
            StringBuilder query = new StringBuilder();
            for (String symbol : symbols) {
                if (query.length() > 0) query.append("&");
                query.append("i=NSE:").append(encode(symbol));
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(KITE_API_BASE + "/quote/ltp?" + query))
                    .header("Authorization", authHeader())
                    .header("X-Kite-Version", "3")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());

            if ("success".equals(root.path("status").asText())) {
                root.path("data").fields().forEachRemaining(entry -> {
                    String key = entry.getKey().replace("NSE:", "");
                    double price = entry.getValue().path("last_price").asDouble();
                    prices.put(key, BigDecimal.valueOf(price));
                });
            }
        } catch (Exception e) {
            log.error("Error fetching live prices from Kite", e);
        }
        return prices;
    }

    public List<KiteHolding> getHoldings() {
        if (accessToken == null) return Collections.emptyList();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(KITE_API_BASE + "/portfolio/holdings"))
                    .header("Authorization", authHeader())
                    .header("X-Kite-Version", "3")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());

            if ("success".equals(root.path("status").asText())) {
                return objectMapper.readValue(
                        root.path("data").toString(),
                        new TypeReference<List<KiteHolding>>() {}
                );
            }
        } catch (Exception e) {
            log.error("Error fetching holdings from Kite", e);
        }
        return Collections.emptyList();
    }

    private String authHeader() {
        return "token " + kiteConfig.getApiKey() + ":" + accessToken;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String sha256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }
}
