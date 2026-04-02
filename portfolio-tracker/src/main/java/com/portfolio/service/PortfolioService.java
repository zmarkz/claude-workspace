package com.portfolio.service;

import com.portfolio.dto.PortfolioMetrics;
import com.portfolio.dto.mapper.HoldingMapper;
import com.portfolio.dto.mapper.PortfolioAnalysisMapper;
import com.portfolio.dto.mapper.PortfolioMapper;
import com.portfolio.dto.request.AddHoldingRequest;
import com.portfolio.dto.request.CreatePortfolioRequest;
import com.portfolio.dto.response.HoldingResponse;
import com.portfolio.dto.response.PortfolioAnalysisResponse;
import com.portfolio.dto.response.PortfolioResponse;
import com.portfolio.entity.Portfolio;
import com.portfolio.entity.PortfolioHolding;
import com.portfolio.entity.Stock;
import com.portfolio.entity.User;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.PortfolioHoldingRepository;
import com.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioHoldingRepository holdingRepository;
    private final StockService stockService;
    private final UserService userService;

    public PortfolioResponse createPortfolio(CreatePortfolioRequest request) {
        User user = userService.getUser(request.getUserId());
        Portfolio portfolio = PortfolioMapper.toEntity(request);
        portfolio.setUser(user);
        Portfolio saved = portfolioRepository.save(portfolio);
        return PortfolioMapper.toResponse(saved);
    }

    public PortfolioResponse getPortfolio(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found"));
        return PortfolioMapper.toResponse(portfolio);
    }

    @Cacheable(value = "portfolioAnalysis", key = "#portfolioId")
    public PortfolioAnalysisResponse getPortfolioAnalysis(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findByIdWithHoldings(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found"));

        Set<Long> stockIds = portfolio.getHoldings().stream()
                .map(h -> h.getStock().getId())
                .collect(Collectors.toSet());

        Map<Long, BigDecimal> currentPrices = stockService.getCurrentPrices(stockIds);
        PortfolioMetrics metrics = calculateMetrics(portfolio, currentPrices);
        return PortfolioAnalysisMapper.toResponse(portfolio, metrics);
    }

    @CacheEvict(value = "portfolioAnalysis", key = "#portfolioId")
    public HoldingResponse addHolding(Long portfolioId, AddHoldingRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found"));
        Stock stock = stockService.getStock(request.getStockId());

        PortfolioHolding holding = holdingRepository
                .findByPortfolioIdAndStockId(portfolioId, stock.getId())
                .map(existing -> {
                    BigDecimal totalCost = existing.getAverageBuyPrice().multiply(existing.getQuantity())
                            .add(request.getBuyPrice().multiply(request.getQuantity()));
                    BigDecimal newQty = existing.getQuantity().add(request.getQuantity());
                    existing.setAverageBuyPrice(totalCost.divide(newQty, 2, RoundingMode.HALF_UP));
                    existing.setQuantity(newQty);
                    return existing;
                })
                .orElseGet(() -> PortfolioHolding.builder()
                        .portfolio(portfolio)
                        .stock(stock)
                        .quantity(request.getQuantity())
                        .averageBuyPrice(request.getBuyPrice())
                        .build());

        PortfolioHolding saved = holdingRepository.save(holding);
        return HoldingMapper.toResponse(saved);
    }

    @CacheEvict(value = "portfolioAnalysis", key = "#portfolioId")
    public void removeHolding(Long portfolioId, Long holdingId) {
        PortfolioHolding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new ResourceNotFoundException("Holding not found"));
        if (!holding.getPortfolio().getId().equals(portfolioId)) {
            throw new ResourceNotFoundException("Holding not in this portfolio");
        }
        holdingRepository.delete(holding);
    }

    public Portfolio getPortfolioEntity(Long id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found"));
    }

    private PortfolioMetrics calculateMetrics(Portfolio portfolio, Map<Long, BigDecimal> currentPrices) {
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalGainLoss = BigDecimal.ZERO;
        Map<String, BigDecimal> sectorAllocation = new HashMap<>();

        for (PortfolioHolding holding : portfolio.getHoldings()) {
            Stock stock = holding.getStock();
            BigDecimal currentPrice = currentPrices.getOrDefault(stock.getId(), stock.getCurrentPrice());
            if (currentPrice == null) currentPrice = BigDecimal.ZERO;

            BigDecimal holdingValue = currentPrice.multiply(holding.getQuantity());
            totalValue = totalValue.add(holdingValue);
            totalGainLoss = totalGainLoss.add(
                    holdingValue.subtract(holding.getAverageBuyPrice().multiply(holding.getQuantity())));

            String sector = stock.getSector() != null ? stock.getSector() : "Other";
            sectorAllocation.merge(sector, holdingValue, BigDecimal::add);
        }

        return new PortfolioMetrics(totalValue, totalGainLoss, sectorAllocation);
    }
}
