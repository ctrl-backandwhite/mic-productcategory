package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.CountryTax;

import java.util.List;

public interface CountryTaxUseCase {

    List<CountryTax> findAll();

    CountryTax findById(String id);

    CountryTax create(CountryTax countryTax);

    CountryTax update(String id, CountryTax countryTax);

    void delete(String id);
}
