package com.backandwhite.api.controller;

import com.backandwhite.api.dto.in.CountryTaxDtoIn;
import com.backandwhite.api.dto.out.CountryTaxDtoOut;
import com.backandwhite.api.dto.out.TaxCalculationDtoOut;
import com.backandwhite.api.mapper.CountryTaxApiMapper;
import com.backandwhite.application.service.TaxCalculationService;
import com.backandwhite.application.usecase.CountryTaxUseCase;
import com.backandwhite.common.security.annotation.NxAdmin;
import com.backandwhite.domain.model.CountryTax;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@NxAdmin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/taxes")
@Tag(name = "Country Taxes", description = "Country tax (VAT) management")
public class CountryTaxController {

    private final CountryTaxUseCase countryTaxUseCase;
    private final TaxCalculationService taxCalculationService;
    private final CountryTaxApiMapper mapper;

    @GetMapping
    @Operation(summary = "List all tax rules")
    public ResponseEntity<List<CountryTaxDtoOut>> findAll() {
        List<CountryTax> taxes = countryTaxUseCase.findAll();
        return ResponseEntity.ok(mapper.toDtoList(taxes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tax rule by ID")
    public ResponseEntity<CountryTaxDtoOut> findById(@PathVariable String id) {
        CountryTax tax = countryTaxUseCase.findById(id);
        return ResponseEntity.ok(mapper.toDto(tax));
    }

    @PostMapping
    @Operation(summary = "Create new tax rule")
    public ResponseEntity<CountryTaxDtoOut> create(@Valid @RequestBody CountryTaxDtoIn dto) {
        CountryTax domain = mapper.toDomain(dto);
        CountryTax created = countryTaxUseCase.create(domain);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update tax rule")
    public ResponseEntity<CountryTaxDtoOut> update(@PathVariable String id, @Valid @RequestBody CountryTaxDtoIn dto) {
        CountryTax domain = mapper.toDomain(dto);
        CountryTax updated = countryTaxUseCase.update(id, domain);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tax rule")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        countryTaxUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/calculate")
    @Operation(summary = "Calculate tax for a subtotal and country")
    public ResponseEntity<TaxCalculationDtoOut> calculate(@RequestParam BigDecimal subtotal,
            @RequestParam String country, @RequestParam(required = false) String state) {

        TaxCalculationService.TaxCalculationResult result = taxCalculationService.calculate(subtotal, country, state);

        TaxCalculationDtoOut dto = TaxCalculationDtoOut.builder().subtotal(result.getSubtotal())
                .taxAmount(result.getTaxAmount()).total(result.getTotal())
                .appliedRates(result.getAppliedRates().stream().map(r -> TaxCalculationDtoOut.AppliedRateDto.builder()
                        .name(r.getName()).rate(r.getRate()).amount(r.getAmount()).build()).toList())
                .build();

        return ResponseEntity.ok(dto);
    }
}
