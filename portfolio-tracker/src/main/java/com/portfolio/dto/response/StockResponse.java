package com.portfolio.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StockResponse {
    private Long id;
    private String symbol;
    private String companyName;
    private String exchange;
    private String sector;
    private BigDecimal currentPrice;
    private LocalDateTime lastUpdatedAt;
}
