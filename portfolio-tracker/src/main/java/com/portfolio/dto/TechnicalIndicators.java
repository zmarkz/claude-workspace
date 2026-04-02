package com.portfolio.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TechnicalIndicators {
    private BigDecimal sma50;
    private BigDecimal sma200;
    private BigDecimal rsi;
    private MACD macd;
}
