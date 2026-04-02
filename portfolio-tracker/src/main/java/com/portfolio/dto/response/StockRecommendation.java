package com.portfolio.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "AI stock recommendation")
public class StockRecommendation {

    @Schema(description = "NSE ticker symbol", example = "RELIANCE")
    private String symbol;

    @Schema(description = "Company name", example = "Reliance Industries Ltd")
    private String companyName;

    @Schema(description = "Recommended action", example = "HOLD", allowableValues = {"BUY", "HOLD", "SELL"})
    private String action;

    @Schema(description = "Rationale for the recommendation")
    private String rationale;

    @Schema(description = "Confidence level", example = "High", allowableValues = {"Low", "Medium", "High"})
    private String confidence;
}
