package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 20, scale = 6)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal price;

    @Column(name = "total_amount", precision = 20, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    private String notes;
}
