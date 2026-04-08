package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.CountryTax;

import java.util.List;
import java.util.Optional;

public interface CountryTaxRepository {

    List<CountryTax> findAll();

    List<CountryTax> findAllActive();

    List<CountryTax> findByCountryCode(String countryCode);

    List<CountryTax> findActiveByCountryCode(String countryCode);

    Optional<CountryTax> findById(String id);

    CountryTax save(CountryTax countryTax);

    CountryTax update(String id, CountryTax countryTax);

    void delete(String id);
}
