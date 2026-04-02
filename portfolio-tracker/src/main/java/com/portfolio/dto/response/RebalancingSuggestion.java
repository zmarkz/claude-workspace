package com.portfolio.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "AI rebalancing suggestion for a single holding")
public class RebalancingSuggestion {

    @Schema(description = "NSE ticker symbol", example = "TCS")
    private String symbol;

    @Schema(description = "Current portfolio weight as percentage", example = "35.5")
    private BigDecimal currentWeightPercent;

    @Schema(description = "Suggested target weight as percentage", example = "20.0")
    private BigDecimal targetWeightPercent;

    @Schema(description = "Action to take", example = "DECREASE", allowableValues = {"INCREASE", "DECREASE", "HOLD"})
    private String action;

    @Schema(description = "Reason for the suggestion")
    private String reason;
}
