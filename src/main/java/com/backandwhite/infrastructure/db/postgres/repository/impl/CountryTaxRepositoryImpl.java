package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.CountryTax;
import com.backandwhite.domain.repository.CountryTaxRepository;
import com.backandwhite.infrastructure.db.postgres.entity.CountryTaxEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.CountryTaxInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.CountryTaxJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CountryTaxRepositoryImpl implements CountryTaxRepository {

    private final CountryTaxJpaRepository jpaRepository;
    private final CountryTaxInfraMapper mapper;

    @Override
    public List<CountryTax> findAll() {
        return mapper.toDomainList(jpaRepository.findAll());
    }

    @Override
    public List<CountryTax> findAllActive() {
        return mapper.toDomainList(jpaRepository.findByActiveTrue());
    }

    @Override
    public List<CountryTax> findByCountryCode(String countryCode) {
        return mapper.toDomainList(jpaRepository.findByCountryCode(countryCode));
    }

    @Override
    public List<CountryTax> findActiveByCountryCode(String countryCode) {
        return mapper.toDomainList(jpaRepository.findByCountryCodeAndActiveTrue(countryCode));
    }

    @Override
    public Optional<CountryTax> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public CountryTax save(CountryTax countryTax) {
        if (countryTax.getId() == null || countryTax.getId().isBlank()) {
            countryTax.setId(UUID.randomUUID().toString());
        }
        CountryTaxEntity entity = mapper.toEntity(countryTax);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public CountryTax update(String id, CountryTax countryTax) {
        CountryTaxEntity existing = jpaRepository.findById(id)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("CountryTax", id));

        existing.setCountryCode(countryTax.getCountryCode());
        existing.setRegion(countryTax.getRegion());
        existing.setRate(countryTax.getRate());
        existing.setType(countryTax.getType());
        existing.setAppliesTo(countryTax.getAppliesTo());
        existing.setIncludesShipping(countryTax.getIncludesShipping());
        existing.setActive(countryTax.getActive());

        return mapper.toDomain(jpaRepository.save(existing));
    }

    @Override
    public void delete(String id) {
        if (!jpaRepository.existsById(id)) {
            throw Message.ENTITY_NOT_FOUND.toEntityNotFound("CountryTax", id);
        }
        jpaRepository.deleteById(id);
    }
}
