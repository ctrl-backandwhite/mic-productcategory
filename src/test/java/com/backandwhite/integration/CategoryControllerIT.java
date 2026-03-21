package com.backandwhite.integration;

import com.backandwhite.api.dto.in.CategoryDtoIn;
import com.backandwhite.api.dto.out.CategoryDtoOut;
import com.backandwhite.config.BaseIntegration;
import com.backandwhite.infrastructure.db.postgres.repository.CategoryJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static com.backandwhite.provider.CategoryProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

class CategoryControllerIT extends BaseIntegration {

    private static final String PATH = "/api/v1/categories";

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Test
    void create_returnsCreatedCategory() {
        CategoryDtoIn dtoIn = categoryDtoIn();

        CategoryDtoOut response = webTestClient
                .post()
                .uri(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dtoIn)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CategoryDtoOut.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotBlank();
        assertThat(response.getLevel()).isEqualTo(CATEGORY_LEVEL);
        assertThat(response.getStatus()).isEqualTo(CATEGORY_STATUS);
        assertThat(response.getActive()).isTrue();
        assertThat(response.getTranslations()).isNotEmpty();
    }

    @Test
    void getById_returnsCategory() {
        CategoryDtoOut created = createCategory();

        CategoryDtoOut response = webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(PATH + "/" + created.getId())
                        .queryParam("locale", "es")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CategoryDtoOut.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(created.getId());
        assertThat(response.getLevel()).isEqualTo(CATEGORY_LEVEL);
        assertThat(response.getName()).isEqualTo(CATEGORY_NAME_ES);
    }

    @Test
    void findByLocale_returnsCategories() {
        createCategory();

        List<CategoryDtoOut> response = webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(PATH)
                        .queryParam("locale", "es")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CategoryDtoOut.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull().isNotEmpty();
        assertThat(response.getFirst().getLevel()).isEqualTo(CATEGORY_LEVEL);
    }

    @Test
    void update_updatesCategory() {
        CategoryDtoOut created = createCategory();

        CategoryDtoIn updateDto = CategoryDtoIn.builder()
                .parentId(null)
                .level(1)
                .translations(List.of(
                        com.backandwhite.api.dto.in.CategoryTranslationDtoIn.builder()
                                .locale("es").name("Tecnología").build()))
                .build();

        CategoryDtoOut response = webTestClient
                .put()
                .uri(PATH + "/" + created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CategoryDtoOut.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(created.getId());
    }

    @Test
    void delete_removesCategory() {
        CategoryDtoOut created = createCategory();

        webTestClient
                .delete()
                .uri(PATH + "/" + created.getId())
                .exchange()
                .expectStatus().isNoContent();

        assertThat(categoryJpaRepository.existsById(created.getId())).isFalse();
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
    void toggleActive_deactivatesCategory() {
        CategoryDtoOut created = createCategory();

        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder.path(PATH + "/" + created.getId() + "/active")
                        .queryParam("active", "false")
                        .build())
                .exchange()
                .expectStatus().isNoContent();

        assertThat(categoryJpaRepository.findById(created.getId()))
                .isPresent()
                .hasValueSatisfying(entity -> assertThat(entity.getActive()).isFalse());
    }

    @Test
    void toggleFeatured_featureCategory() {
        CategoryDtoOut created = createCategory();

        webTestClient
                .patch()
                .uri(uriBuilder -> uriBuilder.path(PATH + "/" + created.getId() + "/featured")
                        .queryParam("featured", "true")
                        .build())
                .exchange()
                .expectStatus().isNoContent();

        assertThat(categoryJpaRepository.findById(created.getId()))
                .isPresent()
                .hasValueSatisfying(entity -> assertThat(entity.getFeatured()).isTrue());
    }

    @Test
    void getById_withEnglishLocale_returnsEnglishName() {
        CategoryDtoOut created = createCategory();

        CategoryDtoOut response = webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(PATH + "/" + created.getId())
                        .queryParam("locale", "en")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CategoryDtoOut.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(CATEGORY_NAME_EN);
    }

    @Test
    void getById_withPortugueseLocale_returnsPortugueseName() {
        CategoryDtoOut created = createCategory();

        CategoryDtoOut response = webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(PATH + "/" + created.getId())
                        .queryParam("locale", "pt-BR")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CategoryDtoOut.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(CATEGORY_NAME_PT);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CategoryDtoOut createCategory() {
        return webTestClient
                .post()
                .uri(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(categoryDtoIn())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CategoryDtoOut.class)
                .returnResult()
                .getResponseBody();
    }
}
