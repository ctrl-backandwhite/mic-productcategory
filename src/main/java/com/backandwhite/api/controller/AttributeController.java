packagecom.backandwhite.api.controller;importcom.backandwhite.common.constants.AppConstants;importcom.backandwhite.common.security.annotation.NxAdmin;importcom.backandwhite.common.security.annotation.NxPublic;importcom.backandwhite.api.dto.PaginationDtoOut;importcom.backandwhite.api.dto.in.AttributeDtoIn;importcom.backandwhite.api.dto.out.AttributeDtoOut;importcom.backandwhite.api.mapper.AttributeApiMapper;importcom.backandwhite.api.util.PageableUtils;importcom.backandwhite.application.usecase.AttributeUseCase;importcom.backandwhite.domain.model.Attribute;importio.swagger.v3.oas.annotations.Operation;importio.swagger.v3.oas.annotations.Parameter;importio.swagger.v3.oas.annotations.tags.Tag;importjakarta.validation.Valid;importlombok.RequiredArgsConstructor;importorg.springframework.data.domain.Page;importorg.springframework.http.HttpStatus;importorg.springframework.http.ResponseEntity;importorg.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/attributes")
@Tag(name = "Attributes",description = "Endpointsparagestióndeatributosdeproducto")publicclassAttributeController {privatefinalAttributeUseCaseattributeUseCase;privatefinalAttributeApiMapperattributeApiMapper;

    // ──Listados ─────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Listaratributospaginados",description = "Devuelveatributospaginadosconsusvalores")publicResponseEntity<PaginationDtoOut<AttributeDtoOut>>findAll(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "Buscarpornombre (parcial,case-insensitive)") @RequestParam(required =false)Stringname,
            @Parameter(description = "Númerodepágina (0-based)",example = "0") @RequestParam(defaultValue = "0")intpage,
            @Parameter(description = "Tamañodepágina",example = "20") @RequestParam(defaultValue = "20")intsize,
            @Parameter(description = "Campodeordenamiento",example = "name") @RequestParam(defaultValue = "name")StringsortBy,
            @Parameter(description = "Ordenascendente",example = "true") @RequestParam(defaultValue = "true")booleanascending) {Page<Attribute>result =attributeUseCase.findAll(name,page,size,sortBy,ascending);returnResponseEntity.ok(PageableUtils.toResponse(result.map(attributeApiMapper::toDto)));
    }

    // ──CRUD ─────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "ObteneratributoporID",description = "Devuelveelatributoconsusvalores")publicResponseEntity<AttributeDtoOut>getById(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "IDdelatributo") @PathVariableStringid) {returnResponseEntity.ok(attributeApiMapper.toDto(attributeUseCase.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Crearatributoconvalores")publicResponseEntity<AttributeDtoOut>create(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Valid @RequestBodyAttributeDtoIndto) {Attributecreated =attributeUseCase.create(attributeApiMapper.toDomain(dto));returnResponseEntity.status(HttpStatus.CREATED).body(attributeApiMapper.toDto(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizaratributo",description = "Sincronizavalores:añadenuevos,actualizaexistentes,eliminalosnopresentes")publicResponseEntity<AttributeDtoOut>update(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringid,
            @Valid @RequestBodyAttributeDtoIndto) {Attributeupdated =attributeUseCase.update(id,attributeApiMapper.toDomain(dto));returnResponseEntity.ok(attributeApiMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminaratributo")publicResponseEntity<Void>delete(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringid) {attributeUseCase.delete(id);returnResponseEntity.noContent().build();
    }
}
