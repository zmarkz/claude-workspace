package com.portfolio.dto.mapper;

import com.portfolio.dto.request.CreateStockRequest;
import com.portfolio.dto.response.StockResponse;
import com.portfolio.entity.Stock;
import org.springframework.stereotype.Component;

@Component
public class StockMapper {

    public static StockResponse toResponse(Stock stock) {
        return StockResponse.builder()
                .id(stock.getId())
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .exchange(stock.getExchange())
                .sector(stock.getSector())
                .currentPrice(stock.getCurrentPrice())
                .lastUpdatedAt(stock.getLastUpdatedAt())
                .build();
    }

    public static Stock toEntity(CreateStockRequest request) {
        return Stock.builder()
                .symbol(request.getSymbol())
                .companyName(request.getCompanyName())
                .exchange(request.getExchange())
                .sector(request.getSector())
                .industry(request.getIndustry())
                .build();
    }
}
