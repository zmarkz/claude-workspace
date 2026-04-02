package com.portfolio.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class HoldingResponse {
    private Long id;
    private String symbol;
    private String companyName;
    private BigDecimal quantity;
    private BigDecimal averageBuyPrice;
    private BigDecimal currentPrice;
    private BigDecimal totalValue;
    private BigDecimal gainLoss;
    private BigDecimal gainLossPercentage;
}

