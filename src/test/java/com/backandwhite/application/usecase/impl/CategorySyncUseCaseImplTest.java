package com.backandwhite.application.usecase.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.domain.model.CategorySyncResult;
import com.backandwhite.domain.repository.CategoryRepository;
import com.backandwhite.infrastructure.client.cj.dto.CjCategoryFirstLevelDto;
import com.backandwhite.infrastructure.client.cj.dto.CjCategorySecondLevelDto;
import com.backandwhite.infrastructure.client.cj.dto.CjCategoryThirdLevelDto;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategorySyncUseCaseImplTest {

    @Mock
    private DropshippingPort cjClient;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategorySyncUseCaseImpl useCase;

    private static CjCategoryFirstLevelDto cat(String firstName, List<CjCategorySecondLevelDto> children) {
        CjCategoryFirstLevelDto c = new CjCategoryFirstLevelDto();
        c.setCategoryFirstName(firstName);
        c.setCategoryFirstList(children);
        return c;
    }

    private static CjCategorySecondLevelDto sec(String secondName, List<CjCategoryThirdLevelDto> third) {
        CjCategorySecondLevelDto s = new CjCategorySecondLevelDto();
        s.setCategorySecondName(secondName);
        s.setCategorySecondList(third);
        return s;
    }

    private static CjCategoryThirdLevelDto thr(String id, String name) {
        CjCategoryThirdLevelDto t = new CjCategoryThirdLevelDto();
        t.setCategoryId(id);
        t.setCategoryName(name);
        return t;
    }

    @Test
    void syncFromCjDropshipping_emptyResponse_returnsZero() {
        when(cjClient.getCategories()).thenReturn(List.of());
        CategorySyncResult result = useCase.syncFromCjDropshipping();
        assertThat(result.getCreated()).isZero();
        assertThat(result.getUpdated()).isZero();
    }

    @Test
    void syncFromCjDropshipping_nestedCategories_created() {
        CjCategoryThirdLevelDto third = thr("cat-3", "Third");
        CjCategorySecondLevelDto second = sec("Second", List.of(third));
        CjCategoryFirstLevelDto first = cat("First", List.of(second));
        when(cjClient.getCategories()).thenReturn(List.of(first));
        when(categoryRepository.findCategoryIdByNameAndLocaleAndLevelAndParent(anyString(), anyString(), anyInt(),
                any())).thenReturn(Optional.empty());
        when(categoryRepository.upsertCategory(any(), any(), anyInt(), anyString(), anyString()))
                .thenReturn("generated-id");
        when(categoryRepository.findCategoryIdById("cat-3")).thenReturn(Optional.empty());

        CategorySyncResult result = useCase.syncFromCjDropshipping();
        assertThat(result.getCreated()).isEqualTo(3);
        verify(categoryRepository).upsertCategory(eq("cat-3"), any(), eq(3), eq("Third"), eq("en"));
    }

    @Test
    void syncFromCjDropshipping_existingCategories_updated() {
        CjCategoryThirdLevelDto third = thr("cat-3", "Third");
        CjCategorySecondLevelDto second = sec("Second", List.of(third));
        CjCategoryFirstLevelDto first = cat("First", List.of(second));
        when(cjClient.getCategories()).thenReturn(List.of(first));
        when(categoryRepository.findCategoryIdByNameAndLocaleAndLevelAndParent(anyString(), anyString(), anyInt(),
                any())).thenReturn(Optional.of("existing"));
        when(categoryRepository.upsertCategory(any(), any(), anyInt(), anyString(), anyString()))
                .thenReturn("generated-id");
        when(categoryRepository.findCategoryIdById("cat-3")).thenReturn(Optional.of("cat-3"));

        CategorySyncResult result = useCase.syncFromCjDropshipping();
        assertThat(result.getUpdated()).isEqualTo(3);
    }

    @Test
    void syncFromCjDropshipping_blankOrNullNames_skipped() {
        CjCategoryThirdLevelDto third = thr("cat-3", " ");
        CjCategorySecondLevelDto second = sec(" ", List.of(third));
        CjCategoryFirstLevelDto first = cat(" ", List.of(second));
        CjCategoryFirstLevelDto nullFirst = cat(null, null);
        when(cjClient.getCategories()).thenReturn(List.of(first, nullFirst));

        CategorySyncResult result = useCase.syncFromCjDropshipping();
        assertThat(result.getTotal()).isZero();
    }

    @Test
    void syncFromCjDropshipping_nullSublists_skipsLevels() {
        CjCategoryFirstLevelDto first = cat("First", null);
        when(cjClient.getCategories()).thenReturn(List.of(first));
        when(categoryRepository.findCategoryIdByNameAndLocaleAndLevelAndParent(anyString(), anyString(), anyInt(),
                any())).thenReturn(Optional.empty());
        when(categoryRepository.upsertCategory(any(), any(), anyInt(), anyString(), anyString()))
                .thenReturn("generated-id");

        CategorySyncResult result = useCase.syncFromCjDropshipping();
        assertThat(result.getCreated()).isEqualTo(1);

        CjCategorySecondLevelDto nullSecond = sec("Second", null);
        CjCategoryFirstLevelDto first2 = cat("First2", List.of(nullSecond));
        when(cjClient.getCategories()).thenReturn(List.of(first2));
        result = useCase.syncFromCjDropshipping();
        assertThat(result.getCreated()).isEqualTo(2);
    }

    @Test
    void syncFromCjDropshipping_thirdLevelWithNullId_skipped() {
        CjCategoryThirdLevelDto third = thr(null, "NullId");
        CjCategorySecondLevelDto second = sec("Second", List.of(third));
        CjCategoryFirstLevelDto first = cat("First", List.of(second));
        when(cjClient.getCategories()).thenReturn(List.of(first));
        when(categoryRepository.findCategoryIdByNameAndLocaleAndLevelAndParent(anyString(), anyString(), anyInt(),
                any())).thenReturn(Optional.empty());
        when(categoryRepository.upsertCategory(any(), any(), anyInt(), anyString(), anyString()))
                .thenReturn("generated-id");

        CategorySyncResult result = useCase.syncFromCjDropshipping();
        assertThat(result.getCreated()).isEqualTo(2);
    }
}
