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
public class ProductDetailVariantTranslationId implements Serializable {

    @Column(name = "vid", length = 64, nullable = false)
    private String vid;

    @Column(name = "locale", length = 5, nullable = false)
    private String locale;
}
