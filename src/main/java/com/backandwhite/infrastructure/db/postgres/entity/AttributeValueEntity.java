package com.backandwhite.infrastructure.db.postgres.entity;

import com.backandwhite.common.infrastructure.entity.AuditableEntity;
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
@Table(name = "attribute_values", indexes = {
        @Index(name = "idx_attribute_values_attribute", columnList = "attribute_id"),
        @Index(name = "idx_attribute_values_position", columnList = "attribute_id, position")})
public class AttributeValueEntity extends AuditableEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "attribute_id", length = 64, nullable = false, insertable = false, updatable = false)
    private String attributeId;

    @Column(name = "value", length = 255, nullable = false)
    private String value;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(name = "position", nullable = false)
    @Builder.Default
    private Integer position = 0;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private AttributeEntity attribute;
}
