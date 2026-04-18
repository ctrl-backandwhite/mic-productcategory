package com.backandwhite.api.controller;

import static com.backandwhite.provider.CategoryProvider.CATEGORY_ID;
import static com.backandwhite.provider.ProductProvider.PRODUCT_ID;
import static com.backandwhite.provider.ProductProvider.product;
import static com.backandwhite.provider.ProductProvider.productDtoOut;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.out.AutocompleteSuggestion;
import com.backandwhite.api.dto.out.BrandDtoOut;
import com.backandwhite.api.dto.out.CategoryDtoOut;
import com.backandwhite.api.dto.out.ProductDetailVariantDtoOut;
import com.backandwhite.api.dto.out.ProductDtoOut;
import com.backandwhite.api.dto.out.ProductSearchResponse;
import com.backandwhite.api.dto.out.ReviewDtoOut;
import com.backandwhite.api.dto.out.ReviewStatsDtoOut;
import com.backandwhite.api.dto.out.StockDtoOut;
import com.backandwhite.api.mapper.BrandApiMapper;
import com.backandwhite.api.mapper.CategoryApiMapper;
import com.backandwhite.api.mapper.ProductApiMapper;
import com.backandwhite.api.mapper.ProductDetailApiMapper;
import com.backandwhite.api.mapper.ReviewApiMapper;
import com.backandwhite.application.service.PricingService;
import com.backandwhite.application.usecase.BrandUseCase;
import com.backandwhite.application.usecase.CategoryUseCase;
import com.backandwhite.application.usecase.InventoryUseCase;
import com.backandwhite.application.usecase.ProductDetailUseCase;
import com.backandwhite.application.usecase.ProductSearchUseCase;
import com.backandwhite.application.usecase.ProductUseCase;
import com.backandwhite.application.usecase.ReviewUseCase;
import com.backandwhite.domain.model.Brand;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.model.Review;
import com.backandwhite.domain.model.ReviewStats;
import com.backandwhite.domain.valueobject.BrandStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class PublicProductControllerTest {

    @Mock
    private ProductUseCase productUseCase;
    @Mock
    private ProductDetailUseCase productDetailUseCase;
    @Mock
    private ProductSearchUseCase productSearchUseCase;
    @Mock
    private CategoryUseCase categoryUseCase;
    @Mock
    private BrandUseCase brandUseCase;
    @Mock
    private ReviewUseCase reviewUseCase;
    @Mock
    private InventoryUseCase inventoryUseCase;
    @Mock
    private PricingService pricingService;
    @Mock
    private ProductApiMapper productApiMapper;
    @Mock
    private ProductDetailApiMapper productDetailApiMapper;
    @Mock
    private CategoryApiMapper categoryApiMapper;
    @Mock
    private BrandApiMapper brandApiMapper;
    @Mock
    private ReviewApiMapper reviewApiMapper;

    @InjectMocks
    private PublicProductController controller;

    @Test
    void listProducts_returnsPagedResponse() {
        Product p = product(CATEGORY_ID);
        Page<Product> page = new PageImpl<>(List.of(p));
        when(productUseCase.findAllPaged(anyString(), any(), anyString(), any(), anyInt(), anyInt(), anyString(),
                anyBoolean())).thenReturn(page);
        when(productApiMapper.toDto(any(Product.class))).thenReturn(productDtoOut(PRODUCT_ID, CATEGORY_ID));

        ResponseEntity<PaginationDtoOut<ProductDtoOut>> response = controller.listProducts("en", null, null, 0, 20,
                "createdAt", false);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void getProduct_returnsProductWithMargins() {
        Product p = product(CATEGORY_ID);
        when(productUseCase.findById(PRODUCT_ID, "en")).thenReturn(p);
        when(productApiMapper.toDto(p)).thenReturn(productDtoOut(PRODUCT_ID, CATEGORY_ID));

        ResponseEntity<ProductDtoOut> response = controller.getProduct(PRODUCT_ID, "en");
        assertThat(response.getBody()).isNotNull();
        verify(pricingService).applyMarginsToProduct(p);
    }

    @Test
    void getProductVariants_returnsVariantList() {
        ProductDetailVariant v = ProductDetailVariant.builder().vid("vid").pid("pid").build();
        when(productDetailUseCase.findVariantsByPid("pid", "en")).thenReturn(List.of(v));
        when(productDetailApiMapper.toVariantDto(v)).thenReturn(new ProductDetailVariantDtoOut());
        ResponseEntity<List<ProductDetailVariantDtoOut>> response = controller.getProductVariants("pid", "en");
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void checkStock_inStock() {
        when(inventoryUseCase.getAvailableStock("vid")).thenReturn(10);
        ResponseEntity<StockDtoOut> response = controller.checkStock("vid");
        assertThat(response.getBody().getAvailable()).isEqualTo(10);
        assertThat(response.getBody().isInStock()).isTrue();
    }

    @Test
    void checkStock_outOfStock() {
        when(inventoryUseCase.getAvailableStock("vid")).thenReturn(0);
        ResponseEntity<StockDtoOut> response = controller.checkStock("vid");
        assertThat(response.getBody().isInStock()).isFalse();
    }

    @Test
    void search_returnsResponse() {
        ProductSearchResponse expected = ProductSearchResponse.builder().build();
        when(productSearchUseCase.search(eq("q"), any(), any(), any(), any(), any(), anyString(), anyInt(), anyInt()))
                .thenReturn(expected);
        ResponseEntity<ProductSearchResponse> response = controller.search("q", null, null, null, null, null,
                "relevance", 0, 24);
        assertThat(response.getBody()).isSameAs(expected);
    }

    @Test
    void autocomplete_returnsSuggestions() {
        when(productSearchUseCase.autocomplete("ph", 5)).thenReturn(List.of(AutocompleteSuggestion.builder().build()));
        ResponseEntity<List<AutocompleteSuggestion>> response = controller.autocomplete("ph", 5);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void listCategories_all() {
        when(categoryUseCase.findCategories("en", null, true)).thenReturn(List.of(Category.builder().build()));
        when(categoryApiMapper.toDto(any(Category.class))).thenReturn(new CategoryDtoOut());
        ResponseEntity<List<CategoryDtoOut>> response = controller.listCategories("en", false);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void listCategories_featured() {
        when(categoryUseCase.findFeatured("en")).thenReturn(List.of(Category.builder().build()));
        when(categoryApiMapper.toDto(any(Category.class))).thenReturn(new CategoryDtoOut());
        ResponseEntity<List<CategoryDtoOut>> response = controller.listCategories("en", true);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getCategory_returnsCategory() {
        Category c = Category.builder().id("id").build();
        when(categoryUseCase.findById("id", "en")).thenReturn(c);
        when(categoryApiMapper.toDto(c)).thenReturn(new CategoryDtoOut());
        ResponseEntity<CategoryDtoOut> response = controller.getCategory("id", "en");
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void listBrands_returnsPaged() {
        Page<Brand> page = new PageImpl<>(List.of(Brand.builder().build()));
        when(brandUseCase.findAll(eq(BrandStatus.ACTIVE), any(), anyInt(), anyInt(), anyString(), anyBoolean()))
                .thenReturn(page);
        when(brandApiMapper.toDto(any(Brand.class))).thenReturn(new BrandDtoOut());

        ResponseEntity<PaginationDtoOut<BrandDtoOut>> response = controller.listBrands(null, 0, 50);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void getProductsByBrand_returnsPagedProducts() {
        Brand brand = Brand.builder().slug("slug").build();
        when(brandUseCase.findBySlug("slug")).thenReturn(brand);
        Page<Product> page = new PageImpl<>(List.of(product(CATEGORY_ID)));
        when(productUseCase.findAllPaged(anyString(), any(), anyString(), any(), anyInt(), anyInt(), anyString(),
                anyBoolean())).thenReturn(page);
        when(productApiMapper.toDto(any(Product.class))).thenReturn(productDtoOut(PRODUCT_ID, CATEGORY_ID));

        ResponseEntity<PaginationDtoOut<ProductDtoOut>> response = controller.getProductsByBrand("slug", "en", 0, 20,
                "createdAt", false);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void getProductReviews_returnsPaged() {
        Page<Review> page = new PageImpl<>(List.of(new Review()));
        when(reviewUseCase.findByProductId(eq("pid"), anyInt(), anyInt(), anyString(), anyBoolean())).thenReturn(page);
        when(reviewApiMapper.toDto(any(Review.class))).thenReturn(new ReviewDtoOut());

        ResponseEntity<PaginationDtoOut<ReviewDtoOut>> response = controller.getProductReviews("pid", 0, 10);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void getReviewStats_returnsStats() {
        ReviewStats stats = ReviewStats.builder().avgRating(4.0).build();
        when(reviewUseCase.getStatsByProductId("pid")).thenReturn(stats);
        when(reviewApiMapper.toStatsDto(stats)).thenReturn(new ReviewStatsDtoOut());
        ResponseEntity<ReviewStatsDtoOut> response = controller.getReviewStats("pid");
        assertThat(response.getBody()).isNotNull();
    }
}
