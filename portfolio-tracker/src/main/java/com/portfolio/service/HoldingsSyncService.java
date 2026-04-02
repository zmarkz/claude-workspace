package com.portfolio.service;

import com.portfolio.dto.kite.KiteHolding;
import com.portfolio.entity.Portfolio;
import com.portfolio.entity.PortfolioHolding;
import com.portfolio.entity.Stock;
import com.portfolio.entity.User;
import com.portfolio.repository.PortfolioHoldingRepository;
import com.portfolio.repository.PortfolioRepository;
import com.portfolio.repository.StockRepository;
import com.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class HoldingsSyncService {

    private final KiteClient kiteClient;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final PortfolioHoldingRepository holdingRepository;
    private final org.springframework.cache.CacheManager cacheManager;

    /**
     * Auto-sync holdings from Kite every 15 minutes for all connected users.
     */
    @Scheduled(fixedRate = 900_000, initialDelay = 60_000) // 15 min, start after 1 min
    public void autoSyncAllUsers() {
        if (!kiteClient.isConfigured()) return;

        List<User> connectedUsers = userRepository.findByKiteAccessTokenIsNotNull();
        if (connectedUsers.isEmpty()) return;

        log.info("Auto-sync: checking {} connected Kite users", connectedUsers.size());

        for (User user : connectedUsers) {
            try {
                List<Portfolio> portfolios = portfolioRepository.findByUserId(user.getId());
                if (portfolios.isEmpty()) continue;

                Portfolio portfolio = portfolios.get(0); // sync to first portfolio
                int synced = syncHoldingsForUser(user, portfolio);
                if (synced > 0) {
                    log.info("Auto-synced {} holdings for user {} into portfolio {}",
                            synced, user.getEmail(), portfolio.getId());
                    // Evict portfolio analysis cache
                    var cache = cacheManager.getCache("portfolioAnalysis");
                    if (cache != null) cache.evict(portfolio.getId());
                }
            } catch (Exception e) {
                log.warn("Auto-sync failed for user {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }

    /**
     * Sync holdings for a specific user into a portfolio. Reusable by both
     * auto-sync and manual sync.
     */
    public int syncHoldingsForUser(User user, Portfolio portfolio) {
        kiteClient.setAccessToken(user.getKiteAccessToken());
        List<KiteHolding> kiteHoldings = kiteClient.getHoldings();

        if (kiteHoldings.isEmpty()) return 0;

        int synced = 0;
        Long portfolioId = portfolio.getId();

        for (KiteHolding kh : kiteHoldings) {
            try {
                String symbol = kh.getTradingsymbol();
                Stock stock = stockRepository.findBySymbol(symbol)
                        .orElseGet(() -> stockRepository.save(Stock.builder()
                                .symbol(symbol)
                                .companyName(symbol)
                                .exchange(kh.getExchange() != null ? kh.getExchange() : "NSE")
                                .currentPrice(BigDecimal.valueOf(kh.getLastPrice()))
                                .lastUpdatedAt(LocalDateTime.now())
                                .build()));

                // Update price from holdings data (previous closing price)
                if (kh.getLastPrice() > 0) {
                    stock.setCurrentPrice(BigDecimal.valueOf(kh.getLastPrice()));
                    stock.setLastUpdatedAt(LocalDateTime.now());
                    stockRepository.save(stock);
                }

                holdingRepository.findByPortfolioIdAndStockId(portfolioId, stock.getId())
                        .ifPresentOrElse(
                                existing -> {
                                    existing.setQuantity(BigDecimal.valueOf(kh.getQuantity()));
                                    existing.setAverageBuyPrice(BigDecimal.valueOf(kh.getAveragePrice()));
                                    holdingRepository.save(existing);
                                },
                                () -> holdingRepository.save(PortfolioHolding.builder()
                                        .portfolio(portfolio)
                                        .stock(stock)
                                        .quantity(BigDecimal.valueOf(kh.getQuantity()))
                                        .averageBuyPrice(BigDecimal.valueOf(kh.getAveragePrice()))
                                        .build())
                        );
                synced++;
            } catch (Exception e) {
                log.warn("Failed to sync {}: {}", kh.getTradingsymbol(), e.getMessage());
            }
        }
        return synced;
    }
}
