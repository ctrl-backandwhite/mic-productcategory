package com.backandwhite.infrastructure.db.postgres.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ProductTranslationId implements Serializable {

    @Column(name = "product_id", length = 64, nullable = false)
    private String productId;

    @Column(name = "locale", length = 5, nullable = false)
    private String locale;
}
