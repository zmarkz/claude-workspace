package com.portfolio.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MACD {
    private BigDecimal macdLine;
    private BigDecimal signalLine;
    private BigDecimal histogram;
}
