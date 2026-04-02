packagecom.backandwhite.api.controller;importcom.backandwhite.common.constants.AppConstants;importcom.backandwhite.common.security.annotation.NxAdmin;importcom.backandwhite.common.security.annotation.NxPublic;importcom.backandwhite.api.dto.PaginationDtoOut;importcom.backandwhite.api.dto.in.WarrantyDtoIn;importcom.backandwhite.api.dto.out.WarrantyDtoOut;importcom.backandwhite.api.mapper.WarrantyApiMapper;importcom.backandwhite.api.util.PageableUtils;importcom.backandwhite.application.usecase.WarrantyUseCase;importcom.backandwhite.domain.model.Warranty;importcom.backandwhite.domain.valureobject.WarrantyType;importio.swagger.v3.oas.annotations.Operation;importio.swagger.v3.oas.annotations.Parameter;importio.swagger.v3.oas.annotations.tags.Tag;importjakarta.validation.Valid;importlombok.RequiredArgsConstructor;importorg.springframework.data.domain.Page;importorg.springframework.http.HttpStatus;importorg.springframework.http.ResponseEntity;importorg.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/warranties")
@Tag(name = "Warranties",description = "Endpointsparagestióndeplanesdegarantía")publicclassWarrantyController {privatefinalWarrantyUseCasewarrantyUseCase;privatefinalWarrantyApiMapperwarrantyApiMapper;

    // ──Listados ─────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Listargarantíaspaginadas",description = "Devuelvegarantíasconfiltrosopcionalesporestadoactivoytipo")publicResponseEntity<PaginationDtoOut<WarrantyDtoOut>>findAll(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "Filtrarporestadoactivo") @RequestParam(required =false)Booleanactive,
            @Parameter(description = "Filtrarportipo (MANUFACTURER,STORE,EXTENDED,LIMITED)") @RequestParam(required =false)WarrantyTypetype,
            @Parameter(description = "Númerodepágina",example = "0") @RequestParam(defaultValue = "0")intpage,
            @Parameter(description = "Tamañodepágina",example = "20") @RequestParam(defaultValue = "20")intsize,
            @Parameter(description = "Campodeordenamiento",example = "name") @RequestParam(defaultValue = "name")StringsortBy,
            @Parameter(description = "Ordenascendente",example = "true") @RequestParam(defaultValue = "true")booleanascending) {Page<Warranty>result =warrantyUseCase.findAll(active,type,page,size,sortBy,ascending);returnResponseEntity.ok(PageableUtils.toResponse(result.map(warrantyApiMapper::toDto)));
    }

    // ──CRUD ─────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "ObtenergarantíaporID")publicResponseEntity<WarrantyDtoOut>getById(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "IDdelagarantía") @PathVariableStringid) {returnResponseEntity.ok(warrantyApiMapper.toDto(warrantyUseCase.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Creargarantía")publicResponseEntity<WarrantyDtoOut>create(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Valid @RequestBodyWarrantyDtoIndto) {Warrantycreated =warrantyUseCase.create(warrantyApiMapper.toDomain(dto));returnResponseEntity.status(HttpStatus.CREATED).body(warrantyApiMapper.toDto(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizargarantía")publicResponseEntity<WarrantyDtoOut>update(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringid,
            @Valid @RequestBodyWarrantyDtoIndto) {Warrantyupdated =warrantyUseCase.update(id,warrantyApiMapper.toDomain(dto));returnResponseEntity.ok(warrantyApiMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminargarantía")publicResponseEntity<Void>delete(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringid) {warrantyUseCase.delete(id);returnResponseEntity.noContent().build();
    }

    // ──Estado ───────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/active")
    @Operation(summary = "Toggleestadoactivo/inactivodelagarantía")publicResponseEntity<Void>toggleActive(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringid) {warrantyUseCase.toggleActive(id);returnResponseEntity.noContent().build();
    }
}
