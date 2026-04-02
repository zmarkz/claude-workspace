package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String exchange;

    private String sector;
    private String industry;

    @Column(name = "current_price", precision = 20, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
    private List<StockPriceHistory> priceHistory = new ArrayList<>();
}
