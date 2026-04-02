package com.portfolio.dto.mapper;

import com.portfolio.dto.request.CreatePortfolioRequest;
import com.portfolio.dto.response.PortfolioResponse;
import com.portfolio.entity.Portfolio;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PortfolioMapper {

    public static PortfolioResponse toResponse(Portfolio portfolio) {
        return PortfolioResponse.builder()
                .id(portfolio.getId())
                .name(portfolio.getName())
                .description(portfolio.getDescription())
                .currency(portfolio.getCurrency())
                .holdings(portfolio.getHoldings().stream()
                        .map(HoldingMapper::toResponse)
                        .collect(Collectors.toList()))
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

    public static Portfolio toEntity(CreatePortfolioRequest request) {
        return Portfolio.builder()
                .name(request.getName())
                .description(request.getDescription())
                .currency(request.getCurrency())
                .build();
    }
}
