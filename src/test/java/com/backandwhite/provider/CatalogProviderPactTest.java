package com.backandwhite.provider;

import static com.backandwhite.provider.CategoryProvider.CATEGORY_ID;
import static com.backandwhite.provider.ProductProvider.PRODUCT_IS_VIDEO;
import static com.backandwhite.provider.ProductProvider.PRODUCT_LISTED_NUM;
import static com.backandwhite.provider.ProductProvider.PRODUCT_SELL_PRICE;
import static com.backandwhite.provider.ProductProvider.PRODUCT_SKU;
import static com.backandwhite.provider.ProductProvider.PRODUCT_TYPE;
import static com.backandwhite.provider.ProductProvider.PRODUCT_WAREHOUSE_NUM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.backandwhite.api.dto.out.CategoryDtoOut;
import com.backandwhite.api.dto.out.CategoryTranslationDtoOut;
import com.backandwhite.api.dto.out.ProductDtoOut;
import com.backandwhite.api.mapper.CategoryApiMapper;
import com.backandwhite.api.mapper.ProductApiMapper;
import com.backandwhite.application.service.PricingService;
import com.backandwhite.application.usecase.CategorySyncUseCase;
import com.backandwhite.application.usecase.CategoryUseCase;
import com.backandwhite.application.usecase.ProductUseCase;
import com.backandwhite.config.TestContainersConfiguration;
import com.backandwhite.core.test.JwtTestUtil;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.Product;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Provider("catalog-service")
@PactFolder("src/test/resources/pacts")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestContainersConfiguration.class, CatalogProviderPactTest.PactAuthConfig.class})
class CatalogProviderPactTest {

    @TestConfiguration
    static class PactAuthConfig {

        @Bean
        FilterRegistrationBean<Filter> pactJwtInjector(JwtTestUtil jwtTestUtil) {
            FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>();
            String token = jwtTestUtil.getToken("pact-test-user", List.of("ADMIN"));
            reg.setFilter((request, response, chain) -> {
                HttpServletRequest httpReq = (HttpServletRequest) request;
                HttpServletRequestWrapper wrapped = new HttpServletRequestWrapper(httpReq) {

                    @Override
                    public String getHeader(String name) {
                        if ("Authorization".equalsIgnoreCase(name)) {
                            return token;
                        }
                        return super.getHeader(name);
                    }

                    @Override
                    public Enumeration<String> getHeaders(String name) {
                        if ("Authorization".equalsIgnoreCase(name)) {
                            return Collections.enumeration(List.of(token));
                        }
                        return super.getHeaders(name);
                    }
                };
                chain.doFilter(wrapped, response);
            });
            reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return reg;
        }
    }

    @LocalServerPort
    private int port;

    @MockitoBean
    CategoryUseCase categoryUseCase;

    @MockitoBean
    CategorySyncUseCase categorySyncUseCase;

    @MockitoBean
    CategoryApiMapper categoryApiMapper;

    @MockitoBean
    ProductUseCase productUseCase;

    @MockitoBean
    ProductApiMapper productApiMapper;

    @MockitoBean
    PricingService pricingService;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPacts(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("product prod-001 exists with category cat-electronics-001")
    void productExists() {
        Product product = Product.builder().id("prod-001").sku(PRODUCT_SKU).categoryId(CATEGORY_ID).name("Test Product")
                .sellPrice(PRODUCT_SELL_PRICE).productType(PRODUCT_TYPE).listedNum(PRODUCT_LISTED_NUM)
                .warehouseInventoryNum(PRODUCT_WAREHOUSE_NUM).isVideo(PRODUCT_IS_VIDEO)
                .createdAt(AuditProvider.CREATED_AT).updatedAt(AuditProvider.UPDATED_AT).build();

        ProductDtoOut dto = ProductDtoOut.builder().id("prod-001").categoryId(CATEGORY_ID).build();

        when(productUseCase.findById(eq("prod-001"), any())).thenReturn(product);
        when(productApiMapper.toDto(any(Product.class))).thenReturn(dto);
    }

    @State("category tree exists with electronics category")
    void categoryTreeExists() {
        Category cat = CategoryProvider.category();

        List<Category> categories = List.of(cat);
        when(categoryUseCase.findCategories(eq("en"), any(), any())).thenReturn(categories);

        CategoryDtoOut categoryDtoOut = CategoryDtoOut.builder().id(CATEGORY_ID).level(CategoryProvider.CATEGORY_LEVEL)
                .active(CategoryProvider.CATEGORY_ACTIVE).featured(CategoryProvider.CATEGORY_FEATURED)
                .name(CategoryProvider.CATEGORY_NAME_EN).translations(List.of(CategoryTranslationDtoOut.builder()
                        .locale("en").name(CategoryProvider.CATEGORY_NAME_EN).build()))
                .subCategories(List.of()).build();

        when(categoryApiMapper.toDtoList(categories)).thenReturn(List.of(categoryDtoOut));
    }
}
