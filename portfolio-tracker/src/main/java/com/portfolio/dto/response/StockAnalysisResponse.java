package com.portfolio.dto.response;

import com.portfolio.dto.TechnicalIndicators;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class StockAnalysisResponse {
    private String symbol;
    private String companyName;
    private BigDecimal currentPrice;
    private TechnicalIndicators indicators;
    private List<String> insights;
}
