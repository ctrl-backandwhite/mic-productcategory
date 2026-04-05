package com.backandwhite.api.controller;

import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.common.security.annotation.NxAdmin;
import com.backandwhite.common.security.annotation.NxPublic;
import com.backandwhite.common.security.annotation.NxUser;
import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.ReviewDtoIn;
import com.backandwhite.api.dto.in.ReviewHelpfulDtoIn;
import com.backandwhite.api.dto.in.ReviewModerateDtoIn;
import com.backandwhite.api.dto.out.ReviewDtoOut;
import com.backandwhite.api.dto.out.ReviewStatsDtoOut;
import com.backandwhite.api.mapper.ReviewApiMapper;
import com.backandwhite.api.util.PageableUtils;
import com.backandwhite.application.usecase.ReviewUseCase;
import com.backandwhite.domain.model.Review;
import com.backandwhite.domain.valureobject.ReviewStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews", description = "Endpoints para gestión de reseñas de productos")
public class ReviewController {

        private final ReviewUseCase reviewUseCase;
        private final ReviewApiMapper reviewApiMapper;

        // ── Público ──────────────────────────────────────────────────────────────

        @GetMapping("/product/{productId}")
        @Operation(summary = "Listar reseñas aprobadas de un producto", description = "Devuelve las reseñas aprobadas paginadas para un producto dado")
        public ResponseEntity<PaginationDtoOut<ReviewDtoOut>> findByProductId(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "ID del producto") @PathVariable String productId,
                        @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Campo de ordenamiento", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Orden ascendente", example = "false") @RequestParam(defaultValue = "false") boolean ascending) {
                Page<Review> result = reviewUseCase.findByProductId(productId, page, size, sortBy, ascending);
                return ResponseEntity.ok(PageableUtils.toResponse(result.map(reviewApiMapper::toDto)));
        }

        @GetMapping("/product/{productId}/stats")
        @Operation(summary = "Obtener estadísticas de reseñas", description = "Devuelve promedio de calificación y distribución por estrellas")
        public ResponseEntity<ReviewStatsDtoOut> getStatsByProductId(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "ID del producto") @PathVariable String productId) {
                return ResponseEntity.ok(reviewApiMapper.toStatsDto(reviewUseCase.getStatsByProductId(productId)));
        }

        @PostMapping("/product/{productId}")
        @Operation(summary = "Crear reseña", description = "Crea una nueva reseña para un producto. Queda en estado PENDING hasta ser moderada")
        public ResponseEntity<ReviewDtoOut> create(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "ID del producto") @PathVariable String productId,
                        @Valid @RequestBody ReviewDtoIn dto) {
                Review review = reviewApiMapper.toDomain(dto);
                review.setProductId(productId);
                Review created = reviewUseCase.create(review);
                return ResponseEntity.status(HttpStatus.CREATED).body(reviewApiMapper.toDto(created));
        }

        @PostMapping("/{id}/helpful")
        @Operation(summary = "Votar reseña como útil", description = "Registra un voto de utilidad (idempotente por sessionId)")
        public ResponseEntity<Void> voteHelpful(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "ID de la reseña") @PathVariable String id,
                        @Valid @RequestBody ReviewHelpfulDtoIn dto) {
                reviewUseCase.voteHelpful(id, dto.getSessionId());
                return ResponseEntity.noContent().build();
        }

        // ── Admin ────────────────────────────────────────────────────────────────

        @GetMapping("/admin")
        @Operation(summary = "Listar todas las reseñas (admin)", description = "Listado paginado con filtros por estado y calificación")
        public ResponseEntity<PaginationDtoOut<ReviewDtoOut>> findAll(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "Filtrar por estado (PENDING, APPROVED, REJECTED)") @RequestParam(required = false) ReviewStatus status,
                        @Parameter(description = "Filtrar por calificación (1-5)") @RequestParam(required = false) Integer rating,
                        @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Campo de ordenamiento", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Orden ascendente", example = "false") @RequestParam(defaultValue = "false") boolean ascending) {
                Page<Review> result = reviewUseCase.findAll(status, rating, page, size, sortBy, ascending);
                return ResponseEntity.ok(PageableUtils.toResponse(result.map(reviewApiMapper::toDto)));
        }

        @PatchMapping("/{id}/moderate")
        @Operation(summary = "Moderar reseña", description = "Cambia el estado de la reseña a APPROVED o REJECTED")
        public ResponseEntity<Void> moderate(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "ID de la reseña") @PathVariable String id,
                        @Valid @RequestBody ReviewModerateDtoIn dto) {
                reviewUseCase.moderate(id, dto.getStatus());
                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Eliminar reseña")
        public ResponseEntity<Void> delete(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "ID de la reseña") @PathVariable String id) {
                reviewUseCase.delete(id);
                return ResponseEntity.noContent().build();
        }
}
