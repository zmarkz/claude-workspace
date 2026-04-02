package com.portfolio.controller;

import com.portfolio.dto.request.ChatRequest;
import com.portfolio.dto.response.PortfolioAIAnalysis;
import com.portfolio.dto.response.RebalancingSuggestion;
import com.portfolio.dto.response.StockRecommendation;
import com.portfolio.exception.ErrorResponse;
import com.portfolio.service.AIAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Portfolio Analysis")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class AIController {

    private final AIAnalysisService aiAnalysisService;

    private ResponseEntity<ErrorResponse> notConfigured() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("AI_NOT_CONFIGURED",
                        "Claude API key not configured. Set the ANTHROPIC_API_KEY environment variable."));
    }

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Check AI configuration status")
    @ApiResponse(responseCode = "200", description = "`{configured: true/false}`")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of("configured", aiAnalysisService.isConfigured()));
    }

    @PostMapping(value = "/portfolio/{id}/analyze", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Full AI portfolio analysis",
            description = "Sends all portfolio holdings to Claude Sonnet 4.6 for analysis. Returns health score, risks, opportunities, and per-stock recommendations."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI analysis result",
                    content = @Content(schema = @Schema(implementation = PortfolioAIAnalysis.class))),
            @ApiResponse(responseCode = "503", description = "Claude API key not configured",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PortfolioAIAnalysis> analyzePortfolio(
            @Parameter(description = "Portfolio ID", example = "1") @PathVariable Long id) {
        if (!aiAnalysisService.isConfigured()) return (ResponseEntity) notConfigured();
        log.info("AI analyzing portfolio: {}", id);
        return ResponseEntity.ok(aiAnalysisService.analyzePortfolio(id));
    }

    @PostMapping(value = "/portfolio/{id}/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Chat with AI about your portfolio (SSE streaming)",
            description = "Conversational interface powered by Claude Sonnet 4.6. Portfolio context injected automatically. Events: `token` (text chunk), `done`, `error`."
    )
    @ApiResponse(responseCode = "200", description = "SSE stream of token chunks")
    public SseEmitter chat(
            @Parameter(description = "Portfolio ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody ChatRequest request) {
        if (!aiAnalysisService.isConfigured()) {
            SseEmitter emitter = new SseEmitter();
            try {
                emitter.send(SseEmitter.event().name("error")
                        .data("Claude API key not configured. Set the ANTHROPIC_API_KEY environment variable."));
                emitter.complete();
            } catch (Exception ignored) {}
            return emitter;
        }
        log.info("AI chat for portfolio: {}", id);
        return aiAnalysisService.chat(id, request);
    }

    @GetMapping(value = "/portfolio/{id}/rebalance", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get AI rebalancing suggestions",
               description = "Claude suggests concrete rebalancing actions to improve sector balance and reduce concentration risk.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of rebalancing suggestions",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RebalancingSuggestion.class)))),
            @ApiResponse(responseCode = "503", description = "Claude API key not configured",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<RebalancingSuggestion>> rebalance(
            @Parameter(description = "Portfolio ID", example = "1") @PathVariable Long id) {
        if (!aiAnalysisService.isConfigured()) return (ResponseEntity) notConfigured();
        log.info("AI rebalancing for portfolio: {}", id);
        return ResponseEntity.ok(aiAnalysisService.getRebalancingSuggestions(id));
    }

    @GetMapping(value = "/portfolio/{portfolioId}/stock/{symbol}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get AI recommendation for a specific stock",
               description = "Returns a BUY / HOLD / SELL recommendation considering the stock's role in the portfolio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock recommendation",
                    content = @Content(schema = @Schema(implementation = StockRecommendation.class))),
            @ApiResponse(responseCode = "503", description = "Claude API key not configured",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StockRecommendation> stockRecommendation(
            @Parameter(description = "Portfolio ID", example = "1") @PathVariable Long portfolioId,
            @Parameter(description = "NSE ticker symbol", example = "RELIANCE") @PathVariable String symbol) {
        if (!aiAnalysisService.isConfigured()) return (ResponseEntity) notConfigured();
        log.info("AI stock recommendation: {} in portfolio {}", symbol, portfolioId);
        return ResponseEntity.ok(aiAnalysisService.getStockRecommendation(symbol, portfolioId));
    }
}
