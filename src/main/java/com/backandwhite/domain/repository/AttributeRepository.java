package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.Attribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AttributeRepository {

    Page<Attribute> findAll(String name, Pageable pageable);

    Optional<Attribute> findById(String attributeId);

    Attribute save(Attribute attribute);

    Attribute update(String attributeId, Attribute attribute);

    void delete(String attributeId);

    boolean existsBySlug(String slug);
}
