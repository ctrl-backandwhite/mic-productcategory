package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.Warranty;
import com.backandwhite.domain.repository.WarrantyRepository;
import com.backandwhite.domain.valueobject.WarrantyType;
import com.backandwhite.infrastructure.db.postgres.entity.WarrantyEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.WarrantyInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.WarrantyJpaRepository;
import com.backandwhite.infrastructure.db.postgres.specification.WarrantySpecification;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarrantyRepositoryImpl implements WarrantyRepository {

    private final WarrantyJpaRepository jpaRepository;
    private final WarrantyInfraMapper mapper;

    @Override
    public Page<Warranty> findAll(Boolean active, WarrantyType type, Pageable pageable) {
        Specification<WarrantyEntity> spec = WarrantySpecification.withFilters(active, type);
        return jpaRepository.findAll(spec, pageable).map(entity -> {
            Warranty warranty = mapper.toDomain(entity);
            warranty.setProductsCount(jpaRepository.countProductsByWarrantyId(entity.getId()));
            return warranty;
        });
    }

    @Override
    public Optional<Warranty> findById(String id) {
        return jpaRepository.findById(id).map(entity -> {
            Warranty warranty = mapper.toDomain(entity);
            warranty.setProductsCount(jpaRepository.countProductsByWarrantyId(entity.getId()));
            return warranty;
        });
    }

    @Override
    public Warranty save(Warranty warranty) {
        warranty.setId(UUID.randomUUID().toString());
        WarrantyEntity entity = mapper.toEntity(warranty);
        Warranty saved = mapper.toDomain(jpaRepository.save(entity));
        saved.setProductsCount(0L);
        return saved;
    }

    @Override
    public Warranty update(Warranty warranty) {
        WarrantyEntity existing = jpaRepository.findById(warranty.getId())
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Warranty", warranty.getId()));

        existing.setName(warranty.getName());
        existing.setType(warranty.getType());
        existing.setDurationMonths(warranty.getDurationMonths());
        existing.setCoverage(warranty.getCoverage());
        existing.setConditions(warranty.getConditions());
        existing.setIncludesLabor(warranty.getIncludesLabor());
        existing.setIncludesParts(warranty.getIncludesParts());
        existing.setIncludesPickup(warranty.getIncludesPickup());
        existing.setRepairLimit(warranty.getRepairLimit());
        existing.setContactPhone(warranty.getContactPhone());
        existing.setContactEmail(warranty.getContactEmail());
        existing.setActive(warranty.getActive());

        Warranty saved = mapper.toDomain(jpaRepository.save(existing));
        saved.setProductsCount(jpaRepository.countProductsByWarrantyId(warranty.getId()));
        return saved;
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.findById(id).orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Warranty", id));
        jpaRepository.deleteById(id);
    }

    @Override
    public long countProductsByWarrantyId(String warrantyId) {
        return jpaRepository.countProductsByWarrantyId(warrantyId);
    }
}
