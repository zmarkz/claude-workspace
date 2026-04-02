package com.portfolio.controller;

import com.portfolio.dto.request.ExecuteTransactionRequest;
import com.portfolio.dto.response.TransactionResponse;
import com.portfolio.exception.ErrorResponse;
import com.portfolio.service.TransactionService;
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

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Transactions")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping(value = "/transactions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Execute a buy or sell transaction",
               description = """
                       Records a trade and updates the portfolio holding:
                       - **BUY**: increases quantity, recalculates weighted average buy price
                       - **SELL**: decreases quantity; throws 422 if insufficient shares
                       """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction recorded",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Insufficient shares for SELL",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio or stock not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TransactionResponse> executeTransaction(
            @Valid @RequestBody ExecuteTransactionRequest request) {
        log.info("Executing {} transaction for portfolio {}", request.getType(), request.getPortfolioId());
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.executeTransaction(request));
    }

    @GetMapping(value = "/portfolios/{portfolioId}/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get transaction history",
               description = "Returns all transactions for a portfolio, ordered by most recent first.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Portfolio not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @Parameter(description = "Portfolio ID", example = "1") @PathVariable Long portfolioId) {
        return ResponseEntity.ok(transactionService.getPortfolioTransactions(portfolioId));
    }
}
