packagecom.backandwhite.api.controller;importcom.backandwhite.common.constants.AppConstants;importcom.backandwhite.common.security.annotation.NxAdmin;importcom.backandwhite.common.security.annotation.NxPublic;importcom.backandwhite.common.security.annotation.NxUser;importcom.backandwhite.api.dto.PaginationDtoOut;importcom.backandwhite.api.dto.in.ReviewDtoIn;importcom.backandwhite.api.dto.in.ReviewHelpfulDtoIn;importcom.backandwhite.api.dto.in.ReviewModerateDtoIn;importcom.backandwhite.api.dto.out.ReviewDtoOut;importcom.backandwhite.api.dto.out.ReviewStatsDtoOut;importcom.backandwhite.api.mapper.ReviewApiMapper;importcom.backandwhite.api.util.PageableUtils;importcom.backandwhite.application.usecase.ReviewUseCase;importcom.backandwhite.domain.model.Review;importcom.backandwhite.domain.valureobject.ReviewStatus;importio.swagger.v3.oas.annotations.Operation;importio.swagger.v3.oas.annotations.Parameter;importio.swagger.v3.oas.annotations.tags.Tag;importjakarta.validation.Valid;importlombok.RequiredArgsConstructor;importorg.springframework.data.domain.Page;importorg.springframework.http.HttpStatus;importorg.springframework.http.ResponseEntity;importorg.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews",description = "Endpointsparagestióndereseñasdeproductos")publicclassReviewController {privatefinalReviewUseCasereviewUseCase;privatefinalReviewApiMapperreviewApiMapper;

    // ──Público ──────────────────────────────────────────────────────────────

    @GetMapping("/product/{productId}")
    @Operation(summary = "Listarreseñasaprobadasdeunproducto",description = "Devuelvelasreseñasaprobadaspaginadasparaunproductodado")publicResponseEntity<PaginationDtoOut<ReviewDtoOut>>findByProductId(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "IDdelproducto") @PathVariableStringproductId,
            @Parameter(description = "Númerodepágina (0-based)",example = "0") @RequestParam(defaultValue = "0")intpage,
            @Parameter(description = "Tamañodepágina",example = "10") @RequestParam(defaultValue = "10")intsize,
            @Parameter(description = "Campodeordenamiento",example = "createdAt") @RequestParam(defaultValue = "createdAt")StringsortBy,
            @Parameter(description = "Ordenascendente",example = "false") @RequestParam(defaultValue = "false")booleanascending) {Page<Review>result =reviewUseCase.findByProductId(productId,page,size,sortBy,ascending);returnResponseEntity.ok(PageableUtils.toResponse(result.map(reviewApiMapper::toDto)));
    }

    @GetMapping("/product/{productId}/stats")
    @Operation(summary = "Obtenerestadísticasdereseñas",description = "Devuelvepromediodecalificaciónydistribuciónporestrellas")publicResponseEntity<ReviewStatsDtoOut>getStatsByProductId(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "IDdelproducto") @PathVariableStringproductId) {returnResponseEntity.ok(reviewApiMapper.toStatsDto(reviewUseCase.getStatsByProductId(productId)));
    }

    @PostMapping("/product/{productId}")
    @Operation(summary = "Crearreseña",description = "Creaunanuevareseñaparaunproducto.QuedaenestadoPENDINGhastasermoderada")publicResponseEntity<ReviewDtoOut>create(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "IDdelproducto") @PathVariableStringproductId,
            @Valid @RequestBodyReviewDtoIndto) {Reviewreview =reviewApiMapper.toDomain(dto);review.setProductId(productId);Reviewcreated =reviewUseCase.create(review);returnResponseEntity.status(HttpStatus.CREATED).body(reviewApiMapper.toDto(created));
    }

    @PostMapping("/{id}/helpful")
    @Operation(summary = "Votarreseñacomo útil",description = "Registraunvotodeutilidad (idempotenteporsessionId)")publicResponseEntity<Void>voteHelpful(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "IDdelareseña") @PathVariableStringid,
            @Valid @RequestBodyReviewHelpfulDtoIndto) {reviewUseCase.voteHelpful(id,dto.getSessionId());returnResponseEntity.noContent().build();
    }

    // ──Admin ────────────────────────────────────────────────────────────────

    @GetMapping("/admin")
    @Operation(summary = "Listartodaslasreseñas (admin)",description = "Listadopaginadoconfiltrosporestadoycalificación")publicResponseEntity<PaginationDtoOut<ReviewDtoOut>>findAll(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "Filtrarporestado (PENDING,APPROVED,REJECTED)") @RequestParam(required =false)ReviewStatusstatus,
            @Parameter(description = "Filtrarporcalificación (1-5)") @RequestParam(required =false)Integerrating,
            @Parameter(description = "Númerodepágina (0-based)",example = "0") @RequestParam(defaultValue = "0")intpage,
            @Parameter(description = "Tamañodepágina",example = "20") @RequestParam(defaultValue = "20")intsize,
            @Parameter(description = "Campodeordenamiento",example = "createdAt") @RequestParam(defaultValue = "createdAt")StringsortBy,
            @Parameter(description = "Ordenascendente",example = "false") @RequestParam(defaultValue = "false")booleanascending) {Page<Review>result =reviewUseCase.findAll(status,rating,page,size,sortBy,ascending);returnResponseEntity.ok(PageableUtils.toResponse(result.map(reviewApiMapper::toDto)));
    }

    @PatchMapping("/{id}/moderate")
    @Operation(summary = "Moderarreseña",description = "CambiaelestadodelareseñaaAPPROVEDoREJECTED")publicResponseEntity<Void>moderate(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "IDdelareseña") @PathVariableStringid,
            @Valid @RequestBodyReviewModerateDtoIndto) {reviewUseCase.moderate(id,dto.getStatus());returnResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminarreseña")publicResponseEntity<Void>delete(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "IDdelareseña") @PathVariableStringid) {reviewUseCase.delete(id);returnResponseEntity.noContent().build();
    }
}
