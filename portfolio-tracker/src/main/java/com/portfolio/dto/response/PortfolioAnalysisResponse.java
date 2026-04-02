package com.portfolio.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PortfolioAnalysisResponse {
    private Long portfolioId;
    private String portfolioName;
    private BigDecimal totalValue;
    private BigDecimal totalGainLoss;
    private BigDecimal gainLossPercent;
    private Map<String, BigDecimal> sectorAllocation;
    private List<HoldingResponse> holdings;
}
