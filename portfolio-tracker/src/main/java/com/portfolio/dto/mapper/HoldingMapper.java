package com.portfolio.dto.mapper;

import com.portfolio.dto.response.HoldingResponse;
import com.portfolio.entity.PortfolioHolding;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class HoldingMapper {

    public static HoldingResponse toResponse(PortfolioHolding holding) {
        BigDecimal currentPrice = holding.getStock().getCurrentPrice() != null
                ? holding.getStock().getCurrentPrice()
                : BigDecimal.ZERO;
        BigDecimal totalValue = currentPrice.multiply(holding.getQuantity());
        BigDecimal costBasis = holding.getAverageBuyPrice().multiply(holding.getQuantity());
        BigDecimal gainLoss = totalValue.subtract(costBasis);
        BigDecimal gainLossPercent = costBasis.compareTo(BigDecimal.ZERO) > 0
                ? gainLoss.divide(costBasis, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return HoldingResponse.builder()
                .id(holding.getId())
                .symbol(holding.getStock().getSymbol())
                .companyName(holding.getStock().getCompanyName())
                .quantity(holding.getQuantity())
                .averageBuyPrice(holding.getAverageBuyPrice())
                .currentPrice(currentPrice)
                .totalValue(totalValue.setScale(2, RoundingMode.HALF_UP))
                .gainLoss(gainLoss.setScale(2, RoundingMode.HALF_UP))
                .gainLossPercentage(gainLossPercent.setScale(2, RoundingMode.HALF_UP))
                .build();
    }
}
