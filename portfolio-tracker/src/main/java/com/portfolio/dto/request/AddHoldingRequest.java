package com.portfolio.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Request to add a stock holding to a portfolio")
public class AddHoldingRequest {

    @NotNull(message = "Stock ID is required")
    @Schema(description = "ID of the stock to add", example = "42")
    private Long stockId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Schema(description = "Number of shares held", example = "10")
    private BigDecimal quantity;

    @NotNull(message = "Buy price is required")
    @Positive(message = "Buy price must be positive")
    @Schema(description = "Average buy price per share in INR", example = "2400.00")
    private BigDecimal buyPrice;
}
