package com.backandwhite.infrastructure.db.postgres.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ProductDetailTranslationId implements Serializable {

    @Column(name = "pid", length = 64, nullable = false)
    private String pid;

    @Column(name = "locale", length = 5, nullable = false)
    private String locale;
}
