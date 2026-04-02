package com.portfolio.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "AI-powered portfolio assessment by Claude Sonnet 4.6")
public class PortfolioAIAnalysis {

    @Schema(description = "Overall portfolio health score from 1 (poor) to 10 (excellent)", example = "7")
    private int healthScore;

    @Schema(description = "One-paragraph summary of the portfolio")
    private String summary;

    @Schema(description = "Risk profile: Low, Medium, or High", example = "Medium")
    private String riskProfile;

    @Schema(description = "List of identified risks")
    private List<String> risks;

    @Schema(description = "List of improvement opportunities")
    private List<String> opportunities;

    @Schema(description = "Per-stock BUY/HOLD/SELL recommendations")
    private List<StockRecommendation> stockRecommendations;
}
