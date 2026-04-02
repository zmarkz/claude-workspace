package com.portfolio.repository;

import com.portfolio.entity.PortfolioHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, Long> {

    List<PortfolioHolding> findByPortfolioId(Long portfolioId);

    Optional<PortfolioHolding> findByPortfolioIdAndStockId(Long portfolioId, Long stockId);

    void deleteByPortfolioId(Long portfolioId);
}
