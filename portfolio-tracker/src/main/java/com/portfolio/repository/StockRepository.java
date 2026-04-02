package com.portfolio.repository;

import com.portfolio.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySymbol(String symbol);

    List<Stock> findBySymbolIn(Set<String> symbols);

    @Query("SELECT s FROM Stock s WHERE UPPER(s.symbol) LIKE UPPER(CONCAT('%', :query, '%')) " +
           "OR UPPER(s.companyName) LIKE UPPER(CONCAT('%', :query, '%'))")
    List<Stock> searchBySymbolOrName(@Param("query") String query, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT DISTINCT s FROM Stock s JOIN PortfolioHolding ph ON ph.stock = s")
    Set<Stock> findAllWithActiveHoldings();
}
