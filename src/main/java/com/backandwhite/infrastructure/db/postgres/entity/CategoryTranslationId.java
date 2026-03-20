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
public class CategoryTranslationId implements Serializable {

    @Column(name = "category_id", length = 64, nullable = false)
    private String categoryId;

    @Column(name = "locale", length = 5, nullable = false)
    private String locale;
}
