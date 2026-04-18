package com.backandwhite.application.usecase.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.backandwhite.application.port.out.CatalogEventPort;
import com.backandwhite.application.port.out.ProductSearchIndexPort;
import com.backandwhite.common.exception.ArgumentException;
import com.backandwhite.common.exception.EntityNotFoundException;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.model.ProductDetailVariantTranslation;
import com.backandwhite.domain.repository.InventoryRepository;
import com.backandwhite.domain.repository.ProductDetailRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryUseCaseImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductDetailRepository productDetailRepository;

    @Mock
    private CatalogEventPort catalogEventPort;

    @Mock
    private ProductSearchIndexPort productSearchIndexPort;

    @InjectMocks
    private InventoryUseCaseImpl inventoryUseCase;

    // ── reserveStock ─────────────────────────────────────────────────────────

    @Test
    void reserveStock_success() {
        when(productDetailRepository.existsVariantByVid("v1")).thenReturn(true);
        when(inventoryRepository.decrementStock("v1", "US", 2)).thenReturn(1);
        when(inventoryRepository.getTotalStockByVid("v1")).thenReturn(8);
        when(productDetailRepository.findVariantByVid(eq("v1"), isNull())).thenReturn(Optional.of(variant("v1", "p1")));
        when(productDetailRepository.findVariantsByPid(eq("p1"), isNull())).thenReturn(List.of(variant("v1", "p1")));

        int remaining = inventoryUseCase.reserveStock("v1", "ord-1", 2, "US");

        assertThat(remaining).isEqualTo(8);
        verify(catalogEventPort).publishStockReserved("p1", "v1", "ord-1", 2, 8);
    }

    @Test
    void reserveStock_insufficientStock_throws() {
        when(productDetailRepository.existsVariantByVid("v1")).thenReturn(true);
        when(inventoryRepository.decrementStock("v1", "GLOBAL", 10)).thenReturn(0);

        assertThatThrownBy(() -> inventoryUseCase.reserveStock("v1", "ord-1", 10, null))
                .isInstanceOf(ArgumentException.class);
    }

    @Test
    void reserveStock_variantNotFound_throws() {
        when(productDetailRepository.existsVariantByVid("missing")).thenReturn(false);

        assertThatThrownBy(() -> inventoryUseCase.reserveStock("missing", "ord-1", 1, null))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void reserveStock_nullCountry_usesGlobal() {
        when(productDetailRepository.existsVariantByVid("v1")).thenReturn(true);
        when(inventoryRepository.decrementStock("v1", "GLOBAL", 1)).thenReturn(1);
        when(inventoryRepository.getTotalStockByVid("v1")).thenReturn(5);
        when(productDetailRepository.findVariantByVid(eq("v1"), isNull())).thenReturn(Optional.of(variant("v1", "p1")));
        when(productDetailRepository.findVariantsByPid(eq("p1"), isNull())).thenReturn(List.of(variant("v1", "p1")));

        int remaining = inventoryUseCase.reserveStock("v1", "ord-1", 1, null);

        assertThat(remaining).isEqualTo(5);
        verify(inventoryRepository).decrementStock("v1", "GLOBAL", 1);
    }

    @Test
    void reserveStock_lowStock_publishesAlert() {
        when(productDetailRepository.existsVariantByVid("v1")).thenReturn(true);
        when(inventoryRepository.decrementStock("v1", "GLOBAL", 1)).thenReturn(1);
        when(inventoryRepository.getTotalStockByVid("v1")).thenReturn(5);
        ProductDetailVariant v = variant("v1", "p1");
        ProductDetailVariantTranslation t = new ProductDetailVariantTranslation();
        t.setVariantName("Red Shirt");
        v.setTranslations(List.of(t));
        when(productDetailRepository.findVariantByVid(eq("v1"), isNull())).thenReturn(Optional.of(v));
        when(productDetailRepository.findVariantsByPid(eq("p1"), isNull())).thenReturn(List.of(v));

        inventoryUseCase.reserveStock("v1", "ord-1", 1, null);

        verify(catalogEventPort).publishStockLowAlert("p1", "v1", "Red Shirt", 5, 10);
    }

    // ── deductStock ──────────────────────────────────────────────────────────

    @Test
    void deductStock_success() {
        when(productDetailRepository.existsVariantByVid("v1")).thenReturn(true);
        when(inventoryRepository.decrementStock("v1", "GLOBAL", 3)).thenReturn(1);
        when(inventoryRepository.getTotalStockByVid("v1")).thenReturn(7);
        when(productDetailRepository.findVariantByVid(eq("v1"), isNull())).thenReturn(Optional.of(variant("v1", "p1")));
        when(productDetailRepository.findVariantsByPid(eq("p1"), isNull())).thenReturn(List.of(variant("v1", "p1")));

        int remaining = inventoryUseCase.deductStock("v1", "ord-1", 3, null);

        assertThat(remaining).isEqualTo(7);
    }

    @Test
    void deductStock_depleted_publishesStockDepleted() {
        when(productDetailRepository.existsVariantByVid("v1")).thenReturn(true);
        when(inventoryRepository.decrementStock("v1", "GLOBAL", 1)).thenReturn(1);
        when(inventoryRepository.getTotalStockByVid("v1")).thenReturn(0);
        ProductDetailVariant v = variant("v1", "p1");
        ProductDetailVariantTranslation t = new ProductDetailVariantTranslation();
        t.setVariantName("Blue Shoe");
        v.setTranslations(List.of(t));
        when(productDetailRepository.findVariantByVid(eq("v1"), isNull())).thenReturn(Optional.of(v));
        when(productDetailRepository.findVariantsByPid(eq("p1"), isNull())).thenReturn(List.of(v));

        inventoryUseCase.deductStock("v1", "ord-1", 1, null);

        verify(catalogEventPort).publishStockDepleted("p1", "v1", "Blue Shoe");
    }

    @Test
    void deductStock_insufficientStock_throws() {
        when(productDetailRepository.existsVariantByVid("v1")).thenReturn(true);
        when(inventoryRepository.decrementStock("v1", "GLOBAL", 100)).thenReturn(0);

        assertThatThrownBy(() -> inventoryUseCase.deductStock("v1", "ord-1", 100, null))
                .isInstanceOf(ArgumentException.class);
    }

    // ── releaseStock ─────────────────────────────────────────────────────────

    @Test
    void releaseStock_success() {
        when(productDetailRepository.existsVariantByVid("v1")).thenReturn(true);
        when(inventoryRepository.getTotalStockByVid("v1")).thenReturn(12);
        when(productDetailRepository.findVariantByVid(eq("v1"), isNull())).thenReturn(Optional.of(variant("v1", "p1")));
        when(productDetailRepository.findVariantsByPid(eq("p1"), isNull())).thenReturn(List.of(variant("v1", "p1")));

        int remaining = inventoryUseCase.releaseStock("v1", "ord-1", 2, null);

        assertThat(remaining).isEqualTo(12);
        verify(inventoryRepository).incrementStock("v1", "GLOBAL", 2);
    }

    // ── restoreStock ─────────────────────────────────────────────────────────

    @Test
    void restoreStock_success() {
        when(productDetailRepository.existsVariantByVid("v1")).thenReturn(true);
        when(inventoryRepository.getTotalStockByVid("v1")).thenReturn(15);
        when(productDetailRepository.findVariantByVid(eq("v1"), isNull())).thenReturn(Optional.of(variant("v1", "p1")));
        when(productDetailRepository.findVariantsByPid(eq("p1"), isNull())).thenReturn(List.of(variant("v1", "p1")));

        int remaining = inventoryUseCase.restoreStock("v1", "ord-1", 5, "US");

        assertThat(remaining).isEqualTo(15);
        verify(inventoryRepository).incrementStock("v1", "US", 5);
    }

    // ── getAvailableStock ────────────────────────────────────────────────────

    @Test
    void getAvailableStock_returnsTotal() {
        when(inventoryRepository.getTotalStockByVid("v1")).thenReturn(42);

        assertThat(inventoryUseCase.getAvailableStock("v1")).isEqualTo(42);
    }

    // ── hasStock ─────────────────────────────────────────────────────────────

    @Test
    void hasStock_sufficient_true() {
        when(inventoryRepository.getTotalStockByVid("v1")).thenReturn(10);

        assertThat(inventoryUseCase.hasStock("v1", 5)).isTrue();
    }

    @Test
    void hasStock_insufficient_false() {
        when(inventoryRepository.getTotalStockByVid("v1")).thenReturn(3);

        assertThat(inventoryUseCase.hasStock("v1", 5)).isFalse();
    }

    @Test
    void hasStock_exact_true() {
        when(inventoryRepository.getTotalStockByVid("v1")).thenReturn(5);

        assertThat(inventoryUseCase.hasStock("v1", 5)).isTrue();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ProductDetailVariant variant(String vid, String pid) {
        ProductDetailVariant v = new ProductDetailVariant();
        v.setVid(vid);
        v.setPid(pid);
        v.setTranslations(List.of());
        return v;
    }
}
