package com.portfolio.controller;

import com.portfolio.dto.response.StockAnalysisResponse;
import com.portfolio.dto.response.StockPriceResponse;
import com.portfolio.dto.response.StockResponse;
import com.portfolio.exception.ErrorResponse;
import com.portfolio.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@Tag(name = "Stock Management")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Search stocks",
               description = "Full-text search by ticker symbol or company name. Returns up to `limit` results.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StockResponse.class))))
    })
    public ResponseEntity<List<StockResponse>> searchStocks(
            @Parameter(description = "Search query (symbol or company name)", example = "RELIANCE")
            @RequestParam String query,
            @Parameter(description = "Max number of results", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Searching stocks with query: {}", query);
        return ResponseEntity.ok(stockService.searchStocks(query, limit));
    }

    @GetMapping(value = "/{symbol}/price", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get current stock price",
               description = "Returns the last cached price for the symbol. Prices are refreshed every 5 seconds from Kite Connect.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current price",
                    content = @Content(schema = @Schema(implementation = StockPriceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Stock not found in database",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StockPriceResponse> getStockPrice(
            @Parameter(description = "NSE ticker symbol", example = "RELIANCE")
            @PathVariable String symbol) {
        log.info("Fetching price for stock: {}", symbol);
        return ResponseEntity.ok(stockService.getStockPrice(symbol));
    }

    @GetMapping(value = "/{symbol}/analysis", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get technical analysis",
               description = """
                       Computes technical indicators over the given date range:
                       - **SMA 50 / SMA 200**: Simple Moving Averages
                       - **RSI (14)**: Relative Strength Index — >70 overbought, <30 oversold
                       - **MACD**: Moving Average Convergence Divergence (12/26/9)

                       Requires at least 26 days of price history in the database for MACD.
                       """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analysis result",
                    content = @Content(schema = @Schema(implementation = StockAnalysisResponse.class))),
            @ApiResponse(responseCode = "404", description = "Stock not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StockAnalysisResponse> getStockAnalysis(
            @Parameter(description = "NSE ticker symbol", example = "TCS") @PathVariable String symbol,
            @Parameter(description = "Start date (ISO 8601)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO 8601)", example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Analyzing stock {} from {} to {}", symbol, startDate, endDate);
        return ResponseEntity.ok(stockService.analyzeStock(symbol, startDate, endDate));
    }
}
