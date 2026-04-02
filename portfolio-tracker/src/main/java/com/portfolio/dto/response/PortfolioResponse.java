package com.portfolio.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PortfolioResponse {
    private Long id;
    private String name;
    private String description;
    private String currency;
    private BigDecimal totalValue;
    private BigDecimal totalGainLoss;
    private List<HoldingResponse> holdings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
