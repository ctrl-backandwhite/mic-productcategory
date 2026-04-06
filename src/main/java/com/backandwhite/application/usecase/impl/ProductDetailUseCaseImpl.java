package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.usecase.ProductDetailUseCase;
import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.*;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.valueobject.ProductStatus;
import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.infrastructure.client.cj.dto.CjProductDetailDto;
import com.backandwhite.infrastructure.client.cj.mapper.CjProductDetailMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProductDetailUseCaseImpl implements ProductDetailUseCase {

    private final ProductDetailRepository productDetailRepository;
    private final DropshippingPort cjDropshippingClient;
    private final CjProductDetailMapper cjProductDetailMapper;

    @Override
    @Transactional
    public ProductDetail getOrFetchFromCj(String pid, String locale) {
        // 1. Verificar si el detalle ya existe en la BD local
        Optional<ProductDetail> existing = productDetailRepository.findByPid(pid);
        if (existing.isPresent()) {
            log.info("ProductDetail pid={} found in local DB, returning cached data", pid);
            return existing.get();
        }

        // 2. No existe -> consultar la API de CJ Dropshipping
        log.info("ProductDetail pid={} not found in DB, fetching from CJ Dropshipping...", pid);
        CjProductDetailDto cjProduct;
        try {
            cjProduct = cjDropshippingClient.getProductDetail(pid);
        } catch (Exception e) {
            log.warn("Failed to fetch product detail from CJ for pid={}: {}", pid, e.getMessage());
            throw Message.ENTITY_NOT_FOUND.toEntityNotFound("ProductDetail", pid);
        }

        if (cjProduct == null || cjProduct.getPid() == null) {
            log.warn("CJ returned null/empty product detail for pid={}", pid);
            throw Message.ENTITY_NOT_FOUND.toEntityNotFound("ProductDetail", pid);
        }

        // 3. Mapear CJ response -> domain model (MapStruct)
        ProductDetail detail = cjProductDetailMapper.toDomain(cjProduct);

        // 4. Persistir en la BD
        log.info("Persisting CJ product detail pid={} (name={}) to local DB...", pid, cjProduct.getProductNameEn());
        productDetailRepository.save(detail);

        // 5. Devolver desde la BD (para asegurar que se devuelve con los datos
        // persistidos)
        return productDetailRepository.findByPid(pid)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("ProductDetail", pid));
    }

    // ── Publish variant ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public void publishVariant(String vid) {
        ProductDetailVariant variant = productDetailRepository.findVariantByVid(vid, null)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("ProductDetailVariant", vid));
        ProductStatus newStatus = variant.getStatus() == ProductStatus.PUBLISHED
                ? ProductStatus.DRAFT
                : ProductStatus.PUBLISHED;
        productDetailRepository.updateVariantStatus(vid, newStatus);
    }

    // ── Variant CRUD ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDetailVariant> findAllVariantsPaged(int page, int size, String locale, String search,
            String status, String pid, String sortBy, boolean ascending) {

        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy.trim() : "createdAt";
        Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        ProductStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            statusEnum = ProductStatus.valueOf(status.toUpperCase());
        }

        return productDetailRepository.findVariantsFiltered(locale, search, statusEnum, pid, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDetailVariant> findVariantsByPid(String pid, String locale) {
        return productDetailRepository.findVariantsByPid(pid, locale);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailVariant findVariantByVid(String vid, String locale) {
        return productDetailRepository.findVariantByVid(vid, locale)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("ProductDetailVariant", vid));
    }

    @Override
    @Transactional
    public ProductDetailVariant createVariant(ProductDetailVariant variant) {
        // Validate that the parent ProductDetail exists
        if (!productDetailRepository.existsByPid(variant.getPid())) {
            throw Message.ENTITY_NOT_FOUND.toEntityNotFound("ProductDetail", variant.getPid());
        }

        // Generate a new UUID for the variant
        String vid = UUID.randomUUID().toString().toUpperCase();
        variant = variant.withVid(vid);

        log.info("Creating new variant vid={} for pid={}", vid, variant.getPid());
        return productDetailRepository.saveVariant(variant);
    }

    @Override
    @Transactional
    public ProductDetailVariant updateVariant(String vid, ProductDetailVariant variant) {
        // Verify variant exists
        ProductDetailVariant existing = productDetailRepository.findVariantByVid(vid, null)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("ProductDetailVariant", vid));

        // Preserve immutable fields
        variant = variant
                .withVid(vid)
                .withPid(existing.getPid())
                .withCreateTime(existing.getCreateTime());

        log.info("Updating variant vid={} for pid={}", vid, existing.getPid());
        return productDetailRepository.saveVariant(variant);
    }

    @Override
    @Transactional
    public void deleteVariant(String vid) {
        if (productDetailRepository.findVariantByVid(vid, null).isEmpty()) {
            throw Message.ENTITY_NOT_FOUND.toEntityNotFound("ProductDetailVariant", vid);
        }
        log.info("Deleting variant vid={}", vid);
        productDetailRepository.deleteVariant(vid);
    }

    @Override
    @Transactional
    public void deleteVariants(List<String> vids) {
        log.info("Bulk deleting {} variants", vids.size());
        productDetailRepository.deleteVariants(vids);
    }

    @Override
    @Transactional
    public void bulkUpdateVariantStatus(List<String> vids, String status) {
        ProductStatus newStatus = ProductStatus.valueOf(status.toUpperCase());
        log.info("Bulk updating {} variants to status={}", vids.size(), newStatus);
        productDetailRepository.bulkUpdateVariantStatus(vids, newStatus);
    }

    @Override
    @Transactional
    public BulkImportResult bulkCreateVariants(List<ProductDetailVariant> variants) {
        int created = 0;
        List<BulkImportResult.RowError> errors = new ArrayList<>();

        for (int i = 0; i < variants.size(); i++) {
            try {
                ProductDetailVariant v = variants.get(i);

                if (!productDetailRepository.existsByPid(v.getPid())) {
                    throw new IllegalArgumentException(
                            "ProductDetail pid=" + v.getPid() + " no existe");
                }

                String vid = UUID.randomUUID().toString().toUpperCase();
                v = v.withVid(vid);

                productDetailRepository.saveVariant(v);
                created++;
            } catch (Exception e) {
                log.warn("Bulk variant row {} failed: {}", i, e.getMessage());
                errors.add(BulkImportResult.RowError.builder()
                        .row(i)
                        .message(e.getMessage())
                        .build());
            }
        }

        log.info("Bulk variant import: created={}, failed={}, total={}", created, errors.size(), variants.size());
        return BulkImportResult.builder()
                .created(created)
                .failed(errors.size())
                .totalRows(variants.size())
                .errors(errors)
                .build();
    }
}
