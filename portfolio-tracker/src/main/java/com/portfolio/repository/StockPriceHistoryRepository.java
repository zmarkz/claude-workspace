package com.portfolio.repository;

import com.portfolio.entity.Stock;
import com.portfolio.entity.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory, Long> {

    List<StockPriceHistory> findByStockAndPriceDateBetweenOrderByPriceDateDesc(
            Stock stock, LocalDate from, LocalDate to);
}
