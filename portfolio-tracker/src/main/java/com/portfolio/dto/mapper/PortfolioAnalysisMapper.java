package com.portfolio.dto.mapper;

import com.portfolio.dto.PortfolioMetrics;
import com.portfolio.dto.response.HoldingResponse;
import com.portfolio.dto.response.PortfolioAnalysisResponse;
import com.portfolio.entity.Portfolio;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PortfolioAnalysisMapper {

    public static PortfolioAnalysisResponse toResponse(Portfolio portfolio, PortfolioMetrics metrics) {
        List<HoldingResponse> holdings = portfolio.getHoldings().stream()
                .map(HoldingMapper::toResponse)
                .collect(Collectors.toList());

        BigDecimal totalCost = portfolio.getHoldings().stream()
                .map(h -> h.getAverageBuyPrice().multiply(h.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gainLossPercent = totalCost.compareTo(BigDecimal.ZERO) > 0
                ? metrics.getTotalGainLoss().divide(totalCost, 4, RoundingMode.HALF_UP)
                         .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return PortfolioAnalysisResponse.builder()
                .portfolioId(portfolio.getId())
                .portfolioName(portfolio.getName())
                .totalValue(metrics.getTotalValue().setScale(2, RoundingMode.HALF_UP))
                .totalGainLoss(metrics.getTotalGainLoss().setScale(2, RoundingMode.HALF_UP))
                .gainLossPercent(gainLossPercent.setScale(2, RoundingMode.HALF_UP))
                .sectorAllocation(metrics.getSectorAllocation())
                .holdings(holdings)
                .build();
    }
}
