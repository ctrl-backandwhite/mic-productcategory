package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.Attribute;
import org.springframework.data.domain.Page;

public interface AttributeUseCase {

    Page<Attribute> findAll(String name, int page, int size, String sortBy, boolean ascending);

    Attribute findById(String attributeId);

    Attribute create(Attribute attribute);

    Attribute update(String attributeId, Attribute attribute);

    void delete(String attributeId);
}
