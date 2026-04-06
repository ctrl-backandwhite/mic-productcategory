package com.backandwhite.infrastructure.db.postgres.entity;

import com.backandwhite.common.infrastructure.entity.AuditableEntity;
import com.backandwhite.domain.valueobject.WarrantyType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@With
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "warranties")
public class WarrantyEntity extends AuditableEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private WarrantyType type;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Column(name = "coverage", columnDefinition = "TEXT")
    private String coverage;

    @Column(name = "conditions", columnDefinition = "TEXT")
    private String conditions;

    @Column(name = "includes_labor", nullable = false)
    private Boolean includesLabor;

    @Column(name = "includes_parts", nullable = false)
    private Boolean includesParts;

    @Column(name = "includes_pickup", nullable = false)
    private Boolean includesPickup;

    @Column(name = "repair_limit")
    private Integer repairLimit;

    @Column(name = "contact_phone", length = 32)
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
