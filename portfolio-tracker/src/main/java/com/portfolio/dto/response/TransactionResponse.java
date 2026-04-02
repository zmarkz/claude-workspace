package com.portfolio.dto.response;

import com.portfolio.entity.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private Long portfolioId;
    private String symbol;
    private String companyName;
    private TransactionType type;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private LocalDateTime transactionDate;
    private String notes;
}
