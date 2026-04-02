package com.portfolio.service;

import com.portfolio.dto.mapper.TransactionMapper;
import com.portfolio.dto.request.ExecuteTransactionRequest;
import com.portfolio.dto.response.TransactionResponse;
import com.portfolio.entity.*;
import com.portfolio.exception.BusinessException;
import com.portfolio.repository.PortfolioHoldingRepository;
import com.portfolio.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final PortfolioHoldingRepository holdingRepository;
    private final StockService stockService;
    private final PortfolioService portfolioService;

    public TransactionResponse executeTransaction(ExecuteTransactionRequest request) {
        Portfolio portfolio = portfolioService.getPortfolioEntity(request.getPortfolioId());
        Stock stock = stockService.getStock(request.getStockId());

        validateTransaction(portfolio, stock, request);

        Transaction transaction = createTransaction(portfolio, stock, request);
        updateHolding(portfolio, stock, request);

        Transaction saved = transactionRepository.save(transaction);
        return TransactionMapper.toResponse(saved);
    }

    public List<TransactionResponse> getPortfolioTransactions(Long portfolioId) {
        return transactionRepository.findByPortfolioIdOrderByTransactionDateDesc(portfolioId)
                .stream()
                .map(TransactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validateTransaction(Portfolio portfolio, Stock stock, ExecuteTransactionRequest request) {
        if (request.getType() == TransactionType.SELL) {
            PortfolioHolding holding = portfolio.getHoldings().stream()
                    .filter(h -> h.getStock().getId().equals(stock.getId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Stock not in portfolio"));

            if (holding.getQuantity().compareTo(request.getQuantity()) < 0) {
                throw new BusinessException("Insufficient shares for sale");
            }
        }
    }

    private Transaction createTransaction(Portfolio portfolio, Stock stock, ExecuteTransactionRequest request) {
        return Transaction.builder()
                .portfolio(portfolio)
                .stock(stock)
                .type(request.getType())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .totalAmount(request.getPrice().multiply(request.getQuantity()))
                .transactionDate(LocalDateTime.now())
                .build();
    }

    private void updateHolding(Portfolio portfolio, Stock stock, ExecuteTransactionRequest request) {
        PortfolioHolding holding = holdingRepository
                .findByPortfolioIdAndStockId(portfolio.getId(), stock.getId())
                .orElseGet(() -> createNewHolding(portfolio, stock));

        updateHoldingQuantity(holding, request.getType(), request.getQuantity(), request.getPrice());
        holdingRepository.save(holding);
    }

    private PortfolioHolding createNewHolding(Portfolio portfolio, Stock stock) {
        return PortfolioHolding.builder()
                .portfolio(portfolio)
                .stock(stock)
                .quantity(BigDecimal.ZERO)
                .averageBuyPrice(BigDecimal.ZERO)
                .build();
    }

    private void updateHoldingQuantity(PortfolioHolding holding, TransactionType type,
                                        BigDecimal qty, BigDecimal price) {
        if (type == TransactionType.BUY) {
            BigDecimal totalCost = holding.getAverageBuyPrice().multiply(holding.getQuantity())
                    .add(price.multiply(qty));
            BigDecimal newQty = holding.getQuantity().add(qty);
            holding.setQuantity(newQty);
            holding.setAverageBuyPrice(newQty.compareTo(BigDecimal.ZERO) > 0
                    ? totalCost.divide(newQty, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
        } else {
            holding.setQuantity(holding.getQuantity().subtract(qty));
        }
    }
}
