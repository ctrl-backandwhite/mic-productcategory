package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.PriceRule;
import com.backandwhite.domain.repository.PriceRuleRepository;
import com.backandwhite.domain.valueobject.PriceRuleScope;
import com.backandwhite.infrastructure.db.postgres.entity.PriceRuleEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.PriceRuleInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.PriceRuleJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PriceRuleRepositoryImpl implements PriceRuleRepository {

    private final PriceRuleJpaRepository jpaRepository;
    private final PriceRuleInfraMapper mapper;

    @Override
    public List<PriceRule> findAll() {
        return mapper.toDomainList(jpaRepository.findAll());
    }

    @Override
    public List<PriceRule> findAllActive() {
        return mapper.toDomainList(jpaRepository.findByActiveTrue());
    }

    @Override
    public Optional<PriceRule> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<PriceRule> findByScopeAndScopeId(PriceRuleScope scope, String scopeId) {
        return jpaRepository.findByScopeAndScopeId(scope, scopeId).map(mapper::toDomain);
    }

    @Override
    public PriceRule save(PriceRule priceRule) {
        if (priceRule.getId() == null || priceRule.getId().isBlank()) {
            priceRule.setId(UUID.randomUUID().toString());
        }
        PriceRuleEntity entity = mapper.toEntity(priceRule);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public PriceRule update(String id, PriceRule priceRule) {
        PriceRuleEntity existing = jpaRepository.findById(id)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("PriceRule", id));

        existing.setScope(priceRule.getScope());
        existing.setScopeId(priceRule.getScopeId());
        existing.setMarginType(priceRule.getMarginType());
        existing.setMarginValue(priceRule.getMarginValue());
        existing.setMinPrice(priceRule.getMinPrice());
        existing.setMaxPrice(priceRule.getMaxPrice());
        existing.setPriority(priceRule.getPriority());
        existing.setActive(priceRule.getActive());

        return mapper.toDomain(jpaRepository.save(existing));
    }

    @Override
    public void delete(String id) {
        if (!jpaRepository.existsById(id)) {
            throw Message.ENTITY_NOT_FOUND.toEntityNotFound("PriceRule", id);
        }
        jpaRepository.deleteById(id);
    }
}
