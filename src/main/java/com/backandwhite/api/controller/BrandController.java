packagecom.backandwhite.api.controller;importcom.backandwhite.common.constants.AppConstants;importcom.backandwhite.common.security.annotation.NxAdmin;importcom.backandwhite.common.security.annotation.NxPublic;importcom.backandwhite.api.dto.PaginationDtoOut;importcom.backandwhite.api.dto.in.BrandDtoIn;importcom.backandwhite.api.dto.out.BrandDtoOut;importcom.backandwhite.api.mapper.BrandApiMapper;importcom.backandwhite.api.util.PageableUtils;importcom.backandwhite.application.usecase.BrandUseCase;importcom.backandwhite.domain.model.Brand;importcom.backandwhite.domain.valureobject.BrandStatus;importio.swagger.v3.oas.annotations.Operation;importio.swagger.v3.oas.annotations.Parameter;importio.swagger.v3.oas.annotations.tags.Tag;importjakarta.validation.Valid;importlombok.RequiredArgsConstructor;importorg.springframework.data.domain.Page;importorg.springframework.http.HttpStatus;importorg.springframework.http.ResponseEntity;importorg.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/brands")
@Tag(name = "Brands",description = "Endpointsparagestióndemarcas")publicclassBrandController {privatefinalBrandUseCasebrandUseCase;privatefinalBrandApiMapperbrandApiMapper;

    // ──Listados ─────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Listarmarcaspaginadas",description = "Devuelvemarcaspaginadasconfiltrosopcionalesporestadoynombre")publicResponseEntity<PaginationDtoOut<BrandDtoOut>>findAll(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "Filtrarporestado (ACTIVE,INACTIVE)") @RequestParam(required =false)BrandStatusstatus,
            @Parameter(description = "Buscarpornombre (parcial,case-insensitive)") @RequestParam(required =false)Stringname,
            @Parameter(description = "Númerodepágina (0-based)",example = "0") @RequestParam(defaultValue = "0")intpage,
            @Parameter(description = "Tamañodepágina",example = "20") @RequestParam(defaultValue = "20")intsize,
            @Parameter(description = "Campodeordenamiento",example = "name") @RequestParam(defaultValue = "name")StringsortBy,
            @Parameter(description = "Ordenascendente",example = "true") @RequestParam(defaultValue = "true")booleanascending) {Page<Brand>result =brandUseCase.findAll(status,name,page,size,sortBy,ascending);returnResponseEntity.ok(PageableUtils.toResponse(result.map(brandApiMapper::toDto)));
    }

    // ──CRUD ─────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "ObtenermarcaporID")publicResponseEntity<BrandDtoOut>getById(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "IDdelamarca") @PathVariableStringid) {returnResponseEntity.ok(brandApiMapper.toDto(brandUseCase.findById(id)));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Obtenermarcaporslug",description = "BuscaunamarcaporsuslugURL-friendly")publicResponseEntity<BrandDtoOut>getBySlug(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "Slugdelamarca",example = "nike") @PathVariableStringslug) {returnResponseEntity.ok(brandApiMapper.toDto(brandUseCase.findBySlug(slug)));
    }

    @PostMapping
    @Operation(summary = "Crearmarca")publicResponseEntity<BrandDtoOut>create(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Valid @RequestBodyBrandDtoIndto) {Brandcreated =brandUseCase.create(brandApiMapper.toDomain(dto));returnResponseEntity.status(HttpStatus.CREATED).body(brandApiMapper.toDto(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizarmarca")publicResponseEntity<BrandDtoOut>update(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringid,
            @Valid @RequestBodyBrandDtoIndto) {Brandupdated =brandUseCase.update(id,brandApiMapper.toDomain(dto));returnResponseEntity.ok(brandApiMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminarmarca")publicResponseEntity<Void>delete(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringid) {brandUseCase.delete(id);returnResponseEntity.noContent().build();
    }

    // ──Estado ───────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiarestadodemarca (ACTIVE ↔INACTIVE)")publicResponseEntity<Void>toggleStatus(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringid) {brandUseCase.toggleStatus(id);returnResponseEntity.noContent().build();
    }
}
