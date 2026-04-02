package com.portfolio.dto.request;

import com.portfolio.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Request to execute a BUY or SELL transaction")
public class ExecuteTransactionRequest {

    @NotNull(message = "Portfolio ID is required")
    @Schema(description = "ID of the portfolio", example = "1")
    private Long portfolioId;

    @NotNull(message = "Stock ID is required")
    @Schema(description = "ID of the stock", example = "42")
    private Long stockId;

    @NotNull(message = "Transaction type is required")
    @Schema(description = "BUY or SELL", example = "BUY")
    private TransactionType type;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Schema(description = "Number of shares", example = "10")
    private BigDecimal quantity;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Schema(description = "Price per share in INR", example = "2450.00")
    private BigDecimal price;
}
