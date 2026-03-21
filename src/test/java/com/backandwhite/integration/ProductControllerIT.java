package com.backandwhite.integration;

import com.backandwhite.api.dto.in.ProductDtoIn;
import com.backandwhite.api.dto.out.CategoryDtoOut;
import com.backandwhite.api.dto.out.ProductDtoOut;
import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.config.BaseIntegration;
import com.backandwhite.infrastructure.db.postgres.repository.ProductJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.List;

import static com.backandwhite.provider.CategoryProvider.categoryDtoIn;
import static com.backandwhite.provider.ProductProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

class ProductControllerIT extends BaseIntegration {

    private static final String CAT_PATH = "/api/v1/categories";
    private static final String PATH = "/api/v1/products";

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Test
    void create_returnsCreatedProduct() {
        String categoryId = createCategory().getId();
        ProductDtoIn dtoIn = productDtoIn(categoryId);

        ProductDtoOut response = webTestClient
                .post()
                .uri(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dtoIn)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductDtoOut.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotBlank();
        assertThat(response.getSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getCategoryId()).isEqualTo(categoryId);
        assertThat(response.getTranslations()).isNotEmpty();
    }

    @Test
    void getById_returnsProduct() {
        String categoryId = createCategory().getId();
        ProductDtoOut created = createProduct(categoryId);

        ProductDtoOut response = webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(PATH + "/" + created.getId())
                        .queryParam("locale", "es")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductDtoOut.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(created.getId());
        assertThat(response.getSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getName()).isEqualTo(PRODUCT_NAME_ES);
    }

    @Test
    void findAllPaged_returnsPagedResults() {
        String categoryId = createCategory().getId();
        createProduct(categoryId);

        PaginationDtoOut<ProductDtoOut> response = webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(PATH)
                        .queryParam("locale", "es")
                        .queryParam("page", "0")
                        .queryParam("size", "20")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PaginationDtoOut<ProductDtoOut>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(response.getContent()).isNotEmpty();
    }

    @Test
    void update_updatesProduct() {
        String categoryId = createCategory().getId();
        ProductDtoOut created = createProduct(categoryId);

        ProductDtoIn updateDto = otherProductDtoIn(categoryId);

        ProductDtoOut response = webTestClient
                .put()
                .uri(PATH + "/" + created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductDtoOut.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(created.getId());
        assertThat(response.getSku()).isEqualTo(OTHER_PRODUCT_SKU);
    }

    @Test
    void deleteAll_removesProducts() {
        String categoryId = createCategory().getId();
        ProductDtoOut created = createProduct(categoryId);

        webTestClient
                .method(org.springframework.http.HttpMethod.DELETE)
                .uri(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(created.getId()))
                .exchange()
                .expectStatus().isNoContent();

        assertThat(productJpaRepository.existsById(created.getId())).isFalse();
    }

    @Test
    void getById_notFound_returns404() {
        webTestClient
                .get()
                .uri(PATH + "/non-existent-id")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void findByCategoryId_returnsProducts() {
        String categoryId = createCategory().getId();
        createProduct(categoryId);

        List<ProductDtoOut> response = webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(PATH + "/category/" + categoryId)
                        .queryParam("locale", "es")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductDtoOut.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull().isNotEmpty();
        assertThat(response.getFirst().getCategoryId()).isEqualTo(categoryId);
    }

    @Test
    void getById_withEnglishLocale_returnsEnglishName() {
        String categoryId = createCategory().getId();
        ProductDtoOut created = createProduct(categoryId);

        ProductDtoOut response = webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(PATH + "/" + created.getId())
                        .queryParam("locale", "en")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductDtoOut.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(PRODUCT_NAME_EN);
    }

    @Test
    void getById_withPortugueseLocale_returnsPortugueseName() {
        String categoryId = createCategory().getId();
        ProductDtoOut created = createProduct(categoryId);

        ProductDtoOut response = webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(PATH + "/" + created.getId())
                        .queryParam("locale", "pt-BR")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductDtoOut.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(PRODUCT_NAME_PT);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CategoryDtoOut createCategory() {
        return webTestClient
                .post()
                .uri(CAT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(categoryDtoIn())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CategoryDtoOut.class)
                .returnResult()
                .getResponseBody();
    }

    private ProductDtoOut createProduct(String categoryId) {
        return webTestClient
                .post()
                .uri(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productDtoIn(categoryId))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductDtoOut.class)
                .returnResult()
                .getResponseBody();
    }
}
