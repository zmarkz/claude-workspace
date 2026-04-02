package com.portfolio.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StockPriceResponse {
    private String symbol;
    private String companyName;
    private BigDecimal currentPrice;
    private LocalDateTime lastUpdatedAt;
}
