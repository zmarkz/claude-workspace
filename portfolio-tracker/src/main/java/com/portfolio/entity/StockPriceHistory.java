package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "stock_price_history")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(name = "open_price", precision = 20, scale = 2)
    private BigDecimal openPrice;

    @Column(name = "high_price", precision = 20, scale = 2)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 20, scale = 2)
    private BigDecimal lowPrice;

    @Column(name = "close_price", precision = 20, scale = 2)
    private BigDecimal closePrice;

    @Column(nullable = false)
    private Long volume;
}
