package com.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
public class PortfolioMetrics {
    private BigDecimal totalValue;
    private BigDecimal totalGainLoss;
    private Map<String, BigDecimal> sectorAllocation;
}
