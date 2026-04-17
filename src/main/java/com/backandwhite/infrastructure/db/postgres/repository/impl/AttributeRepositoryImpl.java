package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.Attribute;
import com.backandwhite.domain.model.AttributeValue;
import com.backandwhite.domain.repository.AttributeRepository;
import com.backandwhite.infrastructure.db.postgres.entity.AttributeEntity;
import com.backandwhite.infrastructure.db.postgres.entity.AttributeValueEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.AttributeInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.AttributeJpaRepository;
import com.backandwhite.infrastructure.db.postgres.specification.AttributeSpecification;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttributeRepositoryImpl implements AttributeRepository {

    private final AttributeJpaRepository attributeJpaRepository;
    private final AttributeInfraMapper attributeInfraMapper;

    @Override
    public Page<Attribute> findAll(String name, Pageable pageable) {
        return attributeJpaRepository.findAll(AttributeSpecification.withFilters(name), pageable)
                .map(attributeInfraMapper::toDomain);
    }

    @Override
    public Optional<Attribute> findById(String attributeId) {
        return attributeJpaRepository.findById(attributeId).map(attributeInfraMapper::toDomain);
    }

    @Override
    public Attribute save(Attribute attribute) {
        String newId = UUID.randomUUID().toString();
        attribute.setId(newId);

        // Assign IDs to values
        if (attribute.getValues() != null) {
            int pos = 0;
            for (AttributeValue v : attribute.getValues()) {
                v.setId(UUID.randomUUID().toString());
                v.setAttributeId(newId);
                v.setPosition(pos++);
            }
        }

        attributeJpaRepository.save(attributeInfraMapper.toEntityWithValues(attribute));

        return findById(newId).orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Attribute", newId));
    }

    @Override
    public Attribute update(String attributeId, Attribute attribute) {
        AttributeEntity entity = findOrThrow(attributeId);

        entity.setName(attribute.getName());
        entity.setSlug(attribute.getSlug());
        entity.setType(attribute.getType());

        syncValues(entity, attribute.getValues());

        attributeJpaRepository.save(entity);

        return findById(attributeId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Attribute", attributeId));
    }

    @Override
    public void delete(String attributeId) {
        attributeJpaRepository.delete(findOrThrow(attributeId));
    }

    @Override
    public boolean existsBySlug(String slug) {
        return attributeJpaRepository.existsBySlug(slug);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private AttributeEntity findOrThrow(String id) {
        return attributeJpaRepository.findById(id)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Attribute", id));
    }

    private void syncValues(AttributeEntity entity, List<AttributeValue> incomingValues) {
        if (incomingValues == null) {
            entity.getValues().clear();
            return;
        }

        // Index existing values by ID
        Map<String, AttributeValueEntity> existingById = entity.getValues().stream().filter(v -> v.getId() != null)
                .collect(Collectors.toMap(AttributeValueEntity::getId, v -> v));

        List<AttributeValueEntity> updatedList = new ArrayList<>();
        int pos = 0;

        for (AttributeValue incoming : incomingValues) {
            if (incoming.getId() != null && existingById.containsKey(incoming.getId())) {
                // Update existing
                AttributeValueEntity existing = existingById.get(incoming.getId());
                existing.setValue(incoming.getValue());
                existing.setColorHex(incoming.getColorHex());
                existing.setPosition(pos++);
                updatedList.add(existing);
            } else {
                // New value
                AttributeValueEntity newEntity = AttributeValueEntity.builder().id(UUID.randomUUID().toString())
                        .value(incoming.getValue()).colorHex(incoming.getColorHex()).position(pos++).attribute(entity)
                        .build();
                updatedList.add(newEntity);
            }
        }

        entity.getValues().clear();
        entity.getValues().addAll(updatedList);
    }
}
