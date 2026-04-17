package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.usecase.CountryTaxUseCase;
import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.CountryTax;
import com.backandwhite.domain.repository.CountryTaxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class CountryTaxUseCaseImpl implements CountryTaxUseCase {

    private final CountryTaxRepository countryTaxRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CountryTax> findAll() {
        return countryTaxRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public CountryTax findById(String id) {
        return countryTaxRepository.findById(id)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("CountryTax", id));
    }

    @Override
    @Transactional
    public CountryTax create(CountryTax countryTax) {
        CountryTax saved = countryTaxRepository.save(countryTax);
        log.info("Country tax created: country={}, rate={}, type={}", saved.getCountryCode(), saved.getRate(),
                saved.getType());
        return saved;
    }

    @Override
    @Transactional
    public CountryTax update(String id, CountryTax countryTax) {
        CountryTax updated = countryTaxRepository.update(id, countryTax);
        log.info("Country tax updated: id={}, country={}, rate={}", id, updated.getCountryCode(), updated.getRate());
        return updated;
    }

    @Override
    @Transactional
    public void delete(String id) {
        countryTaxRepository.delete(id);
        log.info("Country tax deleted: id={}", id);
    }
}
