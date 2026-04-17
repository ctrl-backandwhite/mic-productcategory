package com.backandwhite.api.controller;

import static com.backandwhite.provider.CountryTaxProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.api.dto.in.CountryTaxDtoIn;
import com.backandwhite.api.dto.out.CountryTaxDtoOut;
import com.backandwhite.api.dto.out.TaxCalculationDtoOut;
import com.backandwhite.api.mapper.CountryTaxApiMapper;
import com.backandwhite.application.service.TaxCalculationService;
import com.backandwhite.application.usecase.CountryTaxUseCase;
import com.backandwhite.domain.model.CountryTax;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CountryTaxControllerTest {

    @Mock
    private CountryTaxUseCase countryTaxUseCase;

    @Mock
    private TaxCalculationService taxCalculationService;

    @Mock
    private CountryTaxApiMapper mapper;

    @InjectMocks
    private CountryTaxController controller;

    @Test
    void findAll_returnsTaxList() {
        List<CountryTax> taxes = List.of(countryTax());
        List<CountryTaxDtoOut> dtoOuts = List.of(countryTaxDtoOut());

        when(countryTaxUseCase.findAll()).thenReturn(taxes);
        when(mapper.toDtoList(taxes)).thenReturn(dtoOuts);

        ResponseEntity<List<CountryTaxDtoOut>> response = controller.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOuts);
        verify(countryTaxUseCase).findAll();
        verify(mapper).toDtoList(taxes);
    }

    @Test
    void findById_returnsTax() {
        CountryTax model = countryTax();
        CountryTaxDtoOut dtoOut = countryTaxDtoOut();

        when(countryTaxUseCase.findById(TAX_ID)).thenReturn(model);
        when(mapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<CountryTaxDtoOut> response = controller.findById(TAX_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(countryTaxUseCase).findById(TAX_ID);
        verify(mapper).toDto(model);
    }

    @Test
    void create_returnsCreatedTax() {
        CountryTaxDtoIn dtoIn = countryTaxDtoIn();
        CountryTax model = countryTax();
        CountryTaxDtoOut dtoOut = countryTaxDtoOut();

        when(mapper.toDomain(dtoIn)).thenReturn(model);
        when(countryTaxUseCase.create(model)).thenReturn(model);
        when(mapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<CountryTaxDtoOut> response = controller.create(dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(mapper).toDomain(dtoIn);
        verify(countryTaxUseCase).create(model);
        verify(mapper).toDto(model);
    }

    @Test
    void update_returnsUpdatedTax() {
        CountryTaxDtoIn dtoIn = countryTaxDtoIn();
        CountryTax model = countryTax();
        CountryTaxDtoOut dtoOut = countryTaxDtoOut();

        when(mapper.toDomain(dtoIn)).thenReturn(model);
        when(countryTaxUseCase.update(TAX_ID, model)).thenReturn(model);
        when(mapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<CountryTaxDtoOut> response = controller.update(TAX_ID, dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(mapper).toDomain(dtoIn);
        verify(countryTaxUseCase).update(TAX_ID, model);
        verify(mapper).toDto(model);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> response = controller.delete(TAX_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(countryTaxUseCase).delete(TAX_ID);
    }

    @Test
    void calculate_returnsTaxCalculation() {
        BigDecimal subtotal = new BigDecimal("100.00");
        TaxCalculationService.TaxCalculationResult result = TaxCalculationService.TaxCalculationResult.builder()
                .subtotal(subtotal).taxAmount(new BigDecimal("8.25")).total(new BigDecimal("108.25"))
                .appliedRates(List.of(TaxCalculationService.AppliedRate.builder().name("CA Sales Tax").rate(8.25)
                        .amount(8.25).build()))
                .build();

        when(taxCalculationService.calculate(subtotal, "US", "CA")).thenReturn(result);

        ResponseEntity<TaxCalculationDtoOut> response = controller.calculate(subtotal, "US", "CA");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSubtotal()).isEqualTo(subtotal);
        assertThat(response.getBody().getTaxAmount()).isEqualTo(new BigDecimal("8.25"));
        assertThat(response.getBody().getAppliedRates()).hasSize(1);
        verify(taxCalculationService).calculate(subtotal, "US", "CA");
    }
}
