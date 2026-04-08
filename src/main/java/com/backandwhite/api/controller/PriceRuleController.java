package com.backandwhite.api.controller;

import com.backandwhite.api.dto.in.PriceRuleDtoIn;
import com.backandwhite.api.dto.out.PriceRuleDtoOut;
import com.backandwhite.api.mapper.PriceRuleApiMapper;
import com.backandwhite.application.usecase.PriceRuleUseCase;
import com.backandwhite.domain.model.PriceRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/price-rules")
@Tag(name = "Price Rules", description = "Gestión de reglas de margen de ganancia")
public class PriceRuleController {

    private final PriceRuleUseCase priceRuleUseCase;
    private final PriceRuleApiMapper mapper;

    @GetMapping
    @Operation(summary = "Listar todas las reglas de margen")
    public ResponseEntity<List<PriceRuleDtoOut>> findAll() {
        List<PriceRule> rules = priceRuleUseCase.findAll();
        return ResponseEntity.ok(mapper.toDtoList(rules));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener regla por ID")
    public ResponseEntity<PriceRuleDtoOut> findById(@PathVariable String id) {
        PriceRule rule = priceRuleUseCase.findById(id);
        return ResponseEntity.ok(mapper.toDto(rule));
    }

    @PostMapping
    @Operation(summary = "Crear nueva regla de margen")
    public ResponseEntity<PriceRuleDtoOut> create(@Valid @RequestBody PriceRuleDtoIn dto) {
        PriceRule domain = mapper.toDomain(dto);
        PriceRule created = priceRuleUseCase.create(domain);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar regla de margen")
    public ResponseEntity<PriceRuleDtoOut> update(@PathVariable String id,
            @Valid @RequestBody PriceRuleDtoIn dto) {
        PriceRule domain = mapper.toDomain(dto);
        PriceRule updated = priceRuleUseCase.update(id, domain);
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar regla de margen")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        priceRuleUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
