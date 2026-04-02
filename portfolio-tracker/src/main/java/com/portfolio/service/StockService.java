package com.portfolio.service;

import com.portfolio.dto.MACD;
import com.portfolio.dto.TechnicalIndicators;
import com.portfolio.dto.mapper.StockAnalysisMapper;
import com.portfolio.dto.mapper.StockMapper;
import com.portfolio.dto.response.StockAnalysisResponse;
import com.portfolio.dto.response.StockPriceResponse;
import com.portfolio.dto.response.StockResponse;
import com.portfolio.entity.Stock;
import com.portfolio.entity.StockPriceHistory;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.StockPriceHistoryRepository;
import com.portfolio.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockPriceHistoryRepository priceHistoryRepository;
    private final KiteClient kiteClient;
    private final WebSocketService webSocketService;
    private final CacheManager cacheManager;

    // Disabled: Live quotes API requires paid Kite Connect plan
    // @Scheduled(fixedRate = 5000)
    public void updateLivePrices() {
        try {
            if (!kiteClient.isConfigured()) return;
            Set<String> activeSymbols = getActiveSymbols();
            if (activeSymbols.isEmpty()) return;
            Map<String, BigDecimal> livePrices = kiteClient.getLivePrices(activeSymbols);
            updateStockPrices(livePrices);
            webSocketService.broadcastPriceUpdates(livePrices);
        } catch (Exception e) {
            log.error("Error updating live prices", e);
        }
    }

    public StockAnalysisResponse analyzeStock(String symbol, LocalDate startDate, LocalDate endDate) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found: " + symbol));

        List<StockPriceHistory> priceHistory = getPriceHistory(stock, startDate, endDate);
        TechnicalIndicators indicators = calculateIndicators(priceHistory);
        return StockAnalysisMapper.toResponse(stock, indicators);
    }

    public List<StockResponse> searchStocks(String query, int limit) {
        return stockRepository.searchBySymbolOrName(query, PageRequest.of(0, limit))
                .stream()
                .map(StockMapper::toResponse)
                .collect(Collectors.toList());
    }

    public StockPriceResponse getStockPrice(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found: " + symbol));
        return StockPriceResponse.builder()
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .currentPrice(stock.getCurrentPrice())
                .lastUpdatedAt(stock.getLastUpdatedAt())
                .build();
    }

    public Map<Long, BigDecimal> getCurrentPrices(Set<Long> stockIds) {
        return stockRepository.findAllById(stockIds).stream()
                .filter(s -> s.getCurrentPrice() != null)
                .collect(Collectors.toMap(Stock::getId, Stock::getCurrentPrice));
    }

    public Stock getStock(Long stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found: " + stockId));
    }

    public Stock getStockBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found: " + symbol));
    }

    private Set<String> getActiveSymbols() {
        return stockRepository.findAllWithActiveHoldings()
                .stream()
                .map(Stock::getSymbol)
                .collect(Collectors.toSet());
    }

    private void updateStockPrices(Map<String, BigDecimal> prices) {
        prices.forEach((symbol, price) ->
                stockRepository.findBySymbol(symbol).ifPresent(stock -> {
                    stock.setCurrentPrice(price);
                    stock.setLastUpdatedAt(LocalDateTime.now());
                    stockRepository.save(stock);
                })
        );
    }

    private List<StockPriceHistory> getPriceHistory(Stock stock, LocalDate from, LocalDate to) {
        return priceHistoryRepository.findByStockAndPriceDateBetweenOrderByPriceDateDesc(stock, from, to);
    }

    private TechnicalIndicators calculateIndicators(List<StockPriceHistory> history) {
        return TechnicalIndicators.builder()
                .sma50(calculateSMA(history, 50))
                .sma200(calculateSMA(history, 200))
                .rsi(calculateRSI(history))
                .macd(calculateMACD(history))
                .build();
    }

    private BigDecimal calculateSMA(List<StockPriceHistory> history, int period) {
        if (history.size() < period) return BigDecimal.ZERO;
        return history.stream()
                .limit(period)
                .map(StockPriceHistory::getClosePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(period), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRSI(List<StockPriceHistory> history) {
        if (history.size() < 15) return BigDecimal.ZERO;

        List<BigDecimal> gains = new ArrayList<>();
        List<BigDecimal> losses = new ArrayList<>();

        for (int i = 1; i <= 14; i++) {
            BigDecimal change = history.get(i - 1).getClosePrice()
                    .subtract(history.get(i).getClosePrice());
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains.add(change);
                losses.add(BigDecimal.ZERO);
            } else {
                gains.add(BigDecimal.ZERO);
                losses.add(change.abs());
            }
        }

        BigDecimal avgGain = calculateAverage(gains);
        BigDecimal avgLoss = calculateAverage(losses);

        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) return new BigDecimal("100");

        BigDecimal rs = avgGain.divide(avgLoss, 4, RoundingMode.HALF_UP);
        return new BigDecimal("100")
                .subtract(new BigDecimal("100")
                        .divide(BigDecimal.ONE.add(rs), 2, RoundingMode.HALF_UP));
    }

    private MACD calculateMACD(List<StockPriceHistory> history) {
        if (history.size() < 26) {
            return MACD.builder()
                    .macdLine(BigDecimal.ZERO)
                    .signalLine(BigDecimal.ZERO)
                    .histogram(BigDecimal.ZERO)
                    .build();
        }
        List<BigDecimal> prices = history.stream()
                .map(StockPriceHistory::getClosePrice)
                .collect(Collectors.toList());

        BigDecimal ema12 = calculateEMA(prices, 12);
        BigDecimal ema26 = calculateEMA(prices, 26);
        BigDecimal macdLine = ema12.subtract(ema26);

        List<BigDecimal> macdValues = new ArrayList<>();
        macdValues.add(macdLine);
        BigDecimal signalLine = macdValues.size() >= 9
                ? calculateEMA(macdValues, 9) : macdLine;
        BigDecimal histogram = macdLine.subtract(signalLine);

        return MACD.builder()
                .macdLine(macdLine.setScale(2, RoundingMode.HALF_UP))
                .signalLine(signalLine.setScale(2, RoundingMode.HALF_UP))
                .histogram(histogram.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private BigDecimal calculateEMA(List<BigDecimal> prices, int period) {
        if (prices.size() < period) return calculateAverage(prices);
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));
        BigDecimal ema = calculateAverage(prices.subList(prices.size() - period, prices.size()));
        return ema;
    }

    private BigDecimal calculateAverage(List<BigDecimal> values) {
        if (values.isEmpty()) return BigDecimal.ZERO;
        return values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }
}
