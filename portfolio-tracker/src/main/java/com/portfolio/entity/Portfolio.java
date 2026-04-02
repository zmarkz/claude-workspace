package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "portfolios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(length = 3)
    private String currency;

    @Builder.Default
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    private Set<PortfolioHolding> holdings = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    private List<Transaction> transactions = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
