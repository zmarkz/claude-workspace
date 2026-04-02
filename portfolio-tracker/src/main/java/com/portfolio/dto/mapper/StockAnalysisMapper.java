package com.portfolio.dto.mapper;

import com.portfolio.dto.TechnicalIndicators;
import com.portfolio.dto.response.StockAnalysisResponse;
import com.portfolio.entity.Stock;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockAnalysisMapper {

    public static StockAnalysisResponse toResponse(Stock stock, TechnicalIndicators indicators) {
        return StockAnalysisResponse.builder()
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .currentPrice(stock.getCurrentPrice())
                .indicators(indicators)
                .insights(List.of())
                .build();
    }
}
