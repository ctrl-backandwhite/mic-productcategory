package com.backandwhite.infrastructure.db.postgres.entity;

import com.backandwhite.domain.valueobject.MarginType;
import com.backandwhite.domain.valueobject.PriceRuleScope;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@With
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "price_rules")
public class PriceRuleEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", length = 20, nullable = false)
    private PriceRuleScope scope;

    @Column(name = "scope_id", length = 100)
    private String scopeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "margin_type", length = 20, nullable = false)
    @Builder.Default
    private MarginType marginType = MarginType.PERCENTAGE;

    @Column(name = "margin_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal marginValue;

    @Column(name = "min_price", precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 10, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
            createdAt = Instant.now();
        if (updatedAt == null)
            updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
