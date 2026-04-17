package com.backandwhite.application.usecase.impl;

import static com.backandwhite.provider.CountryTaxProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.common.exception.EntityNotFoundException;
import com.backandwhite.domain.model.CountryTax;
import com.backandwhite.domain.repository.CountryTaxRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CountryTaxUseCaseImplTest {

    @Mock
    private CountryTaxRepository countryTaxRepository;

    @InjectMocks
    private CountryTaxUseCaseImpl countryTaxUseCase;

    @Test
    void findAll_returnsList() {
        List<CountryTax> taxes = List.of(countryTax());
        when(countryTaxRepository.findAll()).thenReturn(taxes);

        List<CountryTax> result = countryTaxUseCase.findAll();

        assertThat(result).hasSize(1);
        assertSame(taxes, result);
    }

    @Test
    void findById_existing_returnsCountryTax() {
        when(countryTaxRepository.findById(TAX_ID)).thenReturn(Optional.of(countryTax()));

        CountryTax result = countryTaxUseCase.findById(TAX_ID);

        assertThat(result.getId()).isEqualTo(TAX_ID);
        assertThat(result.getCountryCode()).isEqualTo(TAX_COUNTRY_CODE);
    }

    @Test
    void findById_missing_throwsEntityNotFoundException() {
        when(countryTaxRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> countryTaxUseCase.findById("non-existent"));
    }

    @Test
    void create_delegatesToRepository() {
        CountryTax model = countryTax();
        when(countryTaxRepository.save(model)).thenReturn(model);

        CountryTax result = countryTaxUseCase.create(model);

        assertSame(model, result);
        verify(countryTaxRepository).save(model);
    }

    @Test
    void update_delegatesToRepository() {
        CountryTax model = countryTax();
        when(countryTaxRepository.update(TAX_ID, model)).thenReturn(model);

        CountryTax result = countryTaxUseCase.update(TAX_ID, model);

        assertSame(model, result);
        verify(countryTaxRepository).update(TAX_ID, model);
    }

    @Test
    void delete_delegatesToRepository() {
        countryTaxUseCase.delete(TAX_ID);

        verify(countryTaxRepository).delete(TAX_ID);
    }
}
