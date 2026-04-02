package com.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.portfolio.config.AnthropicConfig;
import com.portfolio.dto.request.ChatRequest;
import com.portfolio.dto.response.*;
import com.portfolio.entity.Portfolio;
import com.portfolio.entity.PortfolioHolding;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class AIAnalysisService {

    private final AnthropicConfig anthropicConfig;
    private final PortfolioRepository portfolioRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private static final String ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";

    public boolean isConfigured() {
        String key = anthropicConfig.getApiKey();
        return key != null && !key.isBlank();
    }

    public PortfolioAIAnalysis analyzePortfolio(Long portfolioId) {
        Portfolio portfolio = getPortfolio(portfolioId);
        String context = buildPortfolioContext(portfolio);

        String systemPrompt = """
                You are an expert financial advisor specializing in Indian equity markets.
                Analyze the given portfolio and respond in JSON format with this exact structure:
                {
                  "healthScore": <integer 1-10>,
                  "summary": "<brief overall assessment>",
                  "riskProfile": "<Low|Medium|High>",
                  "risks": ["<risk1>", "<risk2>", ...],
                  "opportunities": ["<opportunity1>", ...],
                  "stockRecommendations": [
                    {"symbol":"<symbol>","action":"<BUY|HOLD|SELL>","rationale":"<brief reason>","confidence":"<Low|Medium|High>"},
                    ...
                  ]
                }
                Respond ONLY with valid JSON, no markdown.
                """;

        String userMessage = "Analyze this portfolio:\n\n" + context;
        String response = callClaude(systemPrompt, userMessage, List.of());

        return parsePortfolioAnalysis(response, portfolio);
    }

    public StockRecommendation getStockRecommendation(String symbol, Long portfolioId) {
        Portfolio portfolio = getPortfolio(portfolioId);
        String context = buildPortfolioContext(portfolio);

        String systemPrompt = """
                You are an expert financial advisor for Indian equity markets.
                Provide a buy/hold/sell recommendation in JSON:
                {"symbol":"<symbol>","action":"<BUY|HOLD|SELL>","rationale":"<2-3 sentence explanation>","confidence":"<Low|Medium|High>"}
                Respond ONLY with valid JSON, no markdown.
                """;

        String userMessage = String.format(
                "Give recommendation for %s. Portfolio context:\n%s", symbol, context);
        String response = callClaude(systemPrompt, userMessage, List.of());

        return parseStockRecommendation(response, symbol);
    }

    public List<RebalancingSuggestion> getRebalancingSuggestions(Long portfolioId) {
        Portfolio portfolio = getPortfolio(portfolioId);
        String context = buildPortfolioContext(portfolio);

        String systemPrompt = """
                You are an expert portfolio manager specializing in Indian equities.
                Suggest rebalancing actions in JSON array format:
                [{"symbol":"<symbol>","currentWeightPercent":<number>,"targetWeightPercent":<number>,"action":"<INCREASE|DECREASE|HOLD>","reason":"<brief reason>"}]
                Respond ONLY with valid JSON array, no markdown.
                """;

        String userMessage = "Suggest how to rebalance this portfolio:\n\n" + context;
        String response = callClaude(systemPrompt, userMessage, List.of());

        return parseRebalancingSuggestions(response);
    }

    public SseEmitter chat(Long portfolioId, ChatRequest request) {
        Portfolio portfolio = getPortfolio(portfolioId);
        String context = buildPortfolioContext(portfolio);

        String systemPrompt = String.format("""
                You are an expert financial advisor helping analyze an Indian equity portfolio.
                Be concise, friendly, and actionable. Here is the current portfolio context:

                %s
                """, context);

        List<Map<String, String>> messages = buildMessageHistory(request);
        SseEmitter emitter = new SseEmitter(180_000L);

        executor.submit(() -> {
            try {
                streamClaude(systemPrompt, messages, emitter);
            } catch (Exception e) {
                log.error("SSE streaming error", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("An error occurred"));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private String buildPortfolioContext(Portfolio portfolio) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Portfolio: %s\n", portfolio.getName()));

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        List<String> holdingLines = new ArrayList<>();
        for (PortfolioHolding h : portfolio.getHoldings()) {
            BigDecimal price = h.getStock().getCurrentPrice() != null
                    ? h.getStock().getCurrentPrice() : BigDecimal.ZERO;
            BigDecimal value = price.multiply(h.getQuantity());
            BigDecimal cost = h.getAverageBuyPrice().multiply(h.getQuantity());
            BigDecimal pnl = value.subtract(cost);
            BigDecimal pnlPct = cost.compareTo(BigDecimal.ZERO) > 0
                    ? pnl.divide(cost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            totalValue = totalValue.add(value);
            totalCost = totalCost.add(cost);

            holdingLines.add(String.format("  - %s (%s): qty=%.2f, avgBuy=₹%.2f, currentPrice=₹%.2f, P&L=₹%.2f (%.2f%%), sector=%s",
                    h.getStock().getSymbol(), h.getStock().getCompanyName(),
                    h.getQuantity(), h.getAverageBuyPrice(), price,
                    pnl, pnlPct,
                    h.getStock().getSector() != null ? h.getStock().getSector() : "N/A"));
        }

        BigDecimal totalPnl = totalValue.subtract(totalCost);
        BigDecimal totalPnlPct = totalCost.compareTo(BigDecimal.ZERO) > 0
                ? totalPnl.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        sb.append(String.format("Total Value: ₹%.2f | Total P&L: ₹%.2f (%.2f%%)\n", totalValue, totalPnl, totalPnlPct));
        sb.append("Holdings:\n");
        holdingLines.forEach(line -> sb.append(line).append("\n"));

        return sb.toString();
    }

    private String callClaude(String systemPrompt, String userMessage, List<Map<String, String>> history) {
        if (!isConfigured()) throw new IllegalStateException("Claude API key not configured");
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", anthropicConfig.getModel());
            requestBody.put("max_tokens", anthropicConfig.getMaxTokens());
            requestBody.put("system", systemPrompt);

            ArrayNode messages = requestBody.putArray("messages");
            for (Map<String, String> msg : history) {
                ObjectNode m = messages.addObject();
                m.put("role", msg.get("role"));
                m.put("content", msg.get("content"));
            }
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(ANTHROPIC_URL))
                    .header("x-api-key", anthropicConfig.getApiKey())
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Anthropic API error: {} - {}", response.statusCode(), response.body());
                throw new RuntimeException("Anthropic API error: " + response.statusCode());
            }

            JsonNode responseJson = objectMapper.readTree(response.body());
            return responseJson.path("content").get(0).path("text").asText();

        } catch (Exception e) {
            log.error("Error calling Claude API", e);
            throw new RuntimeException("AI service unavailable", e);
        }
    }

    private void streamClaude(String systemPrompt, List<Map<String, String>> messages, SseEmitter emitter) throws Exception {
        if (!isConfigured()) {
            emitter.send(SseEmitter.event().name("error").data("Claude API key not configured. Set ANTHROPIC_API_KEY environment variable."));
            emitter.complete();
            return;
        }
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", anthropicConfig.getModel());
        requestBody.put("max_tokens", anthropicConfig.getMaxTokens());
        requestBody.put("stream", true);
        requestBody.put("system", systemPrompt);

        ArrayNode messagesNode = requestBody.putArray("messages");
        for (Map<String, String> msg : messages) {
            ObjectNode m = messagesNode.addObject();
            m.put("role", msg.get("role"));
            m.put("content", msg.get("content"));
        }

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ANTHROPIC_URL))
                .header("x-api-key", anthropicConfig.getApiKey())
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<java.io.InputStream> response = httpClient.send(httpRequest,
                HttpResponse.BodyHandlers.ofInputStream());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6).trim();
                    if ("[DONE]".equals(data)) break;
                    try {
                        JsonNode event = objectMapper.readTree(data);
                        String type = event.path("type").asText();
                        if ("content_block_delta".equals(type)) {
                            String text = event.path("delta").path("text").asText();
                            if (!text.isEmpty()) {
                                emitter.send(SseEmitter.event().name("token").data(text));
                            }
                        } else if ("message_stop".equals(type)) {
                            break;
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        emitter.send(SseEmitter.event().name("done").data("[DONE]"));
        emitter.complete();
    }

    private List<Map<String, String>> buildMessageHistory(ChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        if (request.getHistory() != null) {
            for (ChatRequest.ChatMessage msg : request.getHistory()) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }
        messages.add(Map.of("role", "user", "content", request.getMessage()));
        return messages;
    }

    private PortfolioAIAnalysis parsePortfolioAnalysis(String json, Portfolio portfolio) {
        try {
            JsonNode node = objectMapper.readTree(json);
            List<StockRecommendation> recs = new ArrayList<>();
            JsonNode recsNode = node.path("stockRecommendations");
            if (recsNode.isArray()) {
                for (JsonNode r : recsNode) {
                    recs.add(StockRecommendation.builder()
                            .symbol(r.path("symbol").asText())
                            .action(r.path("action").asText())
                            .rationale(r.path("rationale").asText())
                            .confidence(r.path("confidence").asText())
                            .build());
                }
            }
            List<String> risks = new ArrayList<>();
            node.path("risks").forEach(r -> risks.add(r.asText()));
            List<String> opps = new ArrayList<>();
            node.path("opportunities").forEach(o -> opps.add(o.asText()));

            return PortfolioAIAnalysis.builder()
                    .healthScore(node.path("healthScore").asInt(5))
                    .summary(node.path("summary").asText())
                    .riskProfile(node.path("riskProfile").asText("Medium"))
                    .risks(risks)
                    .opportunities(opps)
                    .stockRecommendations(recs)
                    .build();
        } catch (Exception e) {
            log.error("Error parsing portfolio analysis response", e);
            return PortfolioAIAnalysis.builder()
                    .healthScore(5)
                    .summary("Analysis unavailable")
                    .riskProfile("Unknown")
                    .risks(List.of())
                    .opportunities(List.of())
                    .stockRecommendations(List.of())
                    .build();
        }
    }

    private StockRecommendation parseStockRecommendation(String json, String symbol) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return StockRecommendation.builder()
                    .symbol(node.path("symbol").asText(symbol))
                    .action(node.path("action").asText("HOLD"))
                    .rationale(node.path("rationale").asText())
                    .confidence(node.path("confidence").asText("Medium"))
                    .build();
        } catch (Exception e) {
            log.error("Error parsing stock recommendation", e);
            return StockRecommendation.builder().symbol(symbol).action("HOLD")
                    .rationale("Analysis unavailable").confidence("Low").build();
        }
    }

    private List<RebalancingSuggestion> parseRebalancingSuggestions(String json) {
        try {
            JsonNode array = objectMapper.readTree(json);
            List<RebalancingSuggestion> suggestions = new ArrayList<>();
            if (array.isArray()) {
                for (JsonNode node : array) {
                    suggestions.add(RebalancingSuggestion.builder()
                            .symbol(node.path("symbol").asText())
                            .currentWeightPercent(node.path("currentWeightPercent").decimalValue())
                            .targetWeightPercent(node.path("targetWeightPercent").decimalValue())
                            .action(node.path("action").asText())
                            .reason(node.path("reason").asText())
                            .build());
                }
            }
            return suggestions;
        } catch (Exception e) {
            log.error("Error parsing rebalancing suggestions", e);
            return List.of();
        }
    }

    private Portfolio getPortfolio(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found"));
    }
}
