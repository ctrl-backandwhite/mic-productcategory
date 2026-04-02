packagecom.backandwhite.api.controller;importcom.backandwhite.common.constants.AppConstants;importcom.backandwhite.common.security.annotation.NxPublic;importcom.backandwhite.api.dto.PaginationDtoOut;importcom.backandwhite.api.dto.out.*;importcom.backandwhite.api.mapper.BrandApiMapper;importcom.backandwhite.api.mapper.CategoryApiMapper;importcom.backandwhite.api.mapper.ProductApiMapper;importcom.backandwhite.api.mapper.ProductDetailApiMapper;importcom.backandwhite.api.mapper.ReviewApiMapper;importcom.backandwhite.application.usecase.*;importcom.backandwhite.application.usecase.impl.InventoryUseCaseImpl;importcom.backandwhite.domain.model.*;importcom.backandwhite.domain.valureobject.BrandStatus;importio.swagger.v3.oas.annotations.Operation;importio.swagger.v3.oas.annotations.tags.Tag;importlombok.RequiredArgsConstructor;importorg.springframework.data.domain.Page;importorg.springframework.http.ResponseEntity;importorg.springframework.web.bind.annotation.*;importjava.util.List;

/**
 *Publiccatalogendpoints —noauthenticationrequired.
 *Allpathsunder /api/v1/public/**areconfiguredwithoutJWTinthegateway.
 */
@Tag(name = "PublicCatalog",description = "PubliccatalogAPIsforthestorefront (noauth)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public")publicclassPublicProductController {privatefinalProductUseCaseproductUseCase;privatefinalProductDetailUseCaseproductDetailUseCase;privatefinalCategoryUseCasecategoryUseCase;privatefinalBrandUseCasebrandUseCase;privatefinalReviewUseCasereviewUseCase;privatefinalInventoryUseCaseinventoryUseCase;privatefinalProductApiMapperproductApiMapper;privatefinalProductDetailApiMapperproductDetailApiMapper;privatefinalCategoryApiMappercategoryApiMapper;privatefinalBrandApiMapperbrandApiMapper;privatefinalReviewApiMapperreviewApiMapper;

    // ──Products ─────────────────────────────────────────────────────────────

    @Operation(summary = "Listpublishedproductswithfacetedfilters (public)")
    @GetMapping("/products")publicResponseEntity<PaginationDtoOut<ProductDtoOut>>listProducts(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @RequestParam(defaultValue = "en")Stringlocale,
            @RequestParam(required =false)StringcategoryId,
            @RequestParam(required =false)StringbrandId,
            @RequestParam(required =false)Stringname,
            @RequestParam(required =false)DoubleminPrice,
            @RequestParam(required =false)DoublemaxPrice,
            @RequestParam(required =false,defaultValue = "false")booleanfeatured,
            @RequestParam(defaultValue = "0")intpage,
            @RequestParam(defaultValue = "20")intsize,
            @RequestParam(defaultValue = "createdAt")StringsortBy,
            @RequestParam(defaultValue = "false")booleanascending) {Page<Product>products =productUseCase.findAllPagedWithFacets(locale,categoryId, "PUBLISHED",name,brandId,minPrice,maxPrice,page,size,sortBy,ascending);List<ProductDtoOut>content =products.getContent().stream()
                .map(productApiMapper::toDto)
                .toList();returnResponseEntity.ok(PaginationDtoOut.<ProductDtoOut>builder()
                .content(content)
                .currentPage(products.getNumber())
                .pageSize(products.getSize())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .hasNext(products.hasNext())
                .hasPrevious(products.hasPrevious())
                .build());
    }

    @Operation(summary = "GetproductdetailbyID (public)")
    @GetMapping("/products/{id}")publicResponseEntity<ProductDtoOut>getProduct(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringid,
            @RequestParam(defaultValue = "en")Stringlocale) {Productproduct =productUseCase.findById(id,locale);returnResponseEntity.ok(productApiMapper.toDto(product));
    }

    @Operation(summary = "Getproductvariants (public)")
    @GetMapping("/products/{pid}/variants")publicResponseEntity<List<ProductDetailVariantDtoOut>>getProductVariants(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringpid,
            @RequestParam(defaultValue = "en")Stringlocale) {List<ProductDetailVariant>variants =productDetailUseCase.findVariantsByPid(pid,locale);returnResponseEntity.ok(variants.stream()
                .map(productDetailApiMapper::toVariantDto)
                .toList());
    }

    @Operation(summary = "Checkstockavailabilityforavariant (public)")
    @GetMapping("/products/variants/{vid}/stock")publicResponseEntity<StockDtoOut>checkStock(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringvid) {intavailable =inventoryUseCase.getAvailableStock(vid);returnResponseEntity.ok(StockDtoOut.builder()
                .variantId(vid)
                .available(available)
                .inStock(available >0)
                .build());
    }

    @Operation(summary = "Getfacetsforacategory (public)")
    @GetMapping("/products/facets")publicResponseEntity<ProductFacetsDtoOut>getFacets(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @RequestParam(required =false)StringcategoryId) {ProductFacetsfacets =productUseCase.getFacets(categoryId);List<ProductFacetsDtoOut.FacetBrandDto>brandDtos =facets.getBrands().stream()
                .map(b ->ProductFacetsDtoOut.FacetBrandDto.builder()
                        .id(b.getId())
                        .name(b.getName())
                        .count(b.getCount())
                        .build())
                .toList();returnResponseEntity.ok(ProductFacetsDtoOut.builder()
                .brands(brandDtos)
                .priceMin(facets.getPriceMin())
                .priceMax(facets.getPriceMax())
                .build());
    }

    // ──Categories ───────────────────────────────────────────────────────────

    @Operation(summary = "Listactivecategories (public)")
    @GetMapping("/categories")publicResponseEntity<List<CategoryDtoOut>>listCategories(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @RequestParam(defaultValue = "en")Stringlocale,
            @RequestParam(required =false,defaultValue = "false")booleanfeatured) {List<Category>categories;if (featured) {categories =categoryUseCase.findFeatured(locale);
        }else {categories =categoryUseCase.findCategories(locale,null,true);
        }returnResponseEntity.ok(categories.stream()
                .map(categoryApiMapper::toDto)
                .toList());
    }

    @Operation(summary = "GetcategorybyID (public)")
    @GetMapping("/categories/{id}")publicResponseEntity<CategoryDtoOut>getCategory(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringid,
            @RequestParam(defaultValue = "en")Stringlocale) {Categorycategory =categoryUseCase.findById(id,locale);returnResponseEntity.ok(categoryApiMapper.toDto(category));
    }

    // ──Brands ───────────────────────────────────────────────────────────────

    @Operation(summary = "Listactivebrands (public)")
    @GetMapping("/brands")publicResponseEntity<PaginationDtoOut<BrandDtoOut>>listBrands(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @RequestParam(required =false)Stringname,
            @RequestParam(defaultValue = "0")intpage,
            @RequestParam(defaultValue = "50")intsize) {Page<Brand>brands =brandUseCase.findAll(BrandStatus.ACTIVE,name,page,size, "name",true);List<BrandDtoOut>content =brands.getContent().stream()
                .map(brandApiMapper::toDto)
                .toList();returnResponseEntity.ok(PaginationDtoOut.<BrandDtoOut>builder()
                .content(content)
                .currentPage(brands.getNumber())
                .pageSize(brands.getSize())
                .totalElements(brands.getTotalElements())
                .totalPages(brands.getTotalPages())
                .hasNext(brands.hasNext())
                .hasPrevious(brands.hasPrevious())
                .build());
    }

    @Operation(summary = "Getproductsbybrandslug (public)")
    @GetMapping("/brands/{slug}/products")publicResponseEntity<PaginationDtoOut<ProductDtoOut>>getProductsByBrand(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringslug,
            @RequestParam(defaultValue = "en")Stringlocale,
            @RequestParam(defaultValue = "0")intpage,
            @RequestParam(defaultValue = "20")intsize,
            @RequestParam(defaultValue = "createdAt")StringsortBy,
            @RequestParam(defaultValue = "false")booleanascending) {
        //GetbrandbyslugtofindbrandIdBrandbrand =brandUseCase.findBySlug(slug);Page<Product>products =productUseCase.findAllPagedWithFacets(locale,null, "PUBLISHED",null,brand.getId(),null,null,page,size,sortBy,ascending);List<ProductDtoOut>content =products.getContent().stream()
                .map(productApiMapper::toDto)
                .toList();returnResponseEntity.ok(PaginationDtoOut.<ProductDtoOut>builder()
                .content(content)
                .currentPage(products.getNumber())
                .pageSize(products.getSize())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .hasNext(products.hasNext())
                .hasPrevious(products.hasPrevious())
                .build());
    }

    // ──Reviews ──────────────────────────────────────────────────────────────

    @Operation(summary = "Getreviewsforaproduct (public)")
    @GetMapping("/products/{productId}/reviews")publicResponseEntity<PaginationDtoOut<ReviewDtoOut>>getProductReviews(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringproductId,
            @RequestParam(defaultValue = "0")intpage,
            @RequestParam(defaultValue = "10")intsize) {Page<Review>reviews =reviewUseCase.findByProductId(productId,page,size, "createdAt",false);List<ReviewDtoOut>content =reviews.getContent().stream()
                .map(reviewApiMapper::toDto)
                .toList();returnResponseEntity.ok(PaginationDtoOut.<ReviewDtoOut>builder()
                .content(content)
                .currentPage(reviews.getNumber())
                .pageSize(reviews.getSize())
                .totalElements(reviews.getTotalElements())
                .totalPages(reviews.getTotalPages())
                .hasNext(reviews.hasNext())
                .hasPrevious(reviews.hasPrevious())
                .build());
    }

    @Operation(summary = "Getreviewstatsforaproduct (public)")
    @GetMapping("/products/{productId}/reviews/stats")publicResponseEntity<ReviewStatsDtoOut>getReviewStats(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringproductId) {ReviewStatsstats =reviewUseCase.getStatsByProductId(productId);returnResponseEntity.ok(reviewApiMapper.toStatsDto(stats));
    }
}
