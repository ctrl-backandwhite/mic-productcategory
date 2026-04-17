package com.backandwhite.infrastructure.db.postgres.entity;

import com.backandwhite.domain.valueobject.TaxType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@With
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "country_taxes", indexes = {@Index(name = "idx_country_taxes_country", columnList = "country_code"),
        @Index(name = "idx_country_taxes_active", columnList = "active")})
public class CountryTaxEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "country_code", length = 3, nullable = false)
    private String countryCode;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "rate", precision = 8, scale = 6, nullable = false)
    private BigDecimal rate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    @Builder.Default
    private TaxType type = TaxType.PERCENTAGE;

    @Column(name = "applies_to", columnDefinition = "TEXT")
    private String appliesTo;

    @Column(name = "includes_shipping", nullable = false)
    @Builder.Default
    private Boolean includesShipping = true;

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
