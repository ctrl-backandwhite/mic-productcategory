package com.backandwhite.application.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.repository.CategoryRepository;
import com.backandwhite.domain.repository.DiscoveredPidRepository;
import com.backandwhite.domain.repository.DiscoveryStateRepository;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListPageDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListV2ContentDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListV2ItemDto;
import com.backandwhite.infrastructure.configuration.CjDropshippingProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiscoveryByCategoryStrategyTest {

    @Mock
    private DropshippingPort dropshippingPort;
    @Mock
    private DiscoveredPidRepository discoveredPidRepository;
    @Mock
    private ProductDetailRepository productDetailRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private DiscoveryStateRepository stateRepository;

    private CjDropshippingProperties properties;
    private DiscoveryByCategoryStrategy strategy;

    @BeforeEach
    void setUp() {
        properties = new CjDropshippingProperties();
        properties.getDiscovery().setMaxPagesPerCategory(1);
        properties.getDiscovery().setPageSize(10);
        properties.getDiscovery().setRateLimitWaitMs(0L);
        strategy = new DiscoveryByCategoryStrategy(dropshippingPort, discoveredPidRepository, productDetailRepository,
                categoryRepository, stateRepository, properties);
    }

    private static CjProductListPageDto pageWith(int total, CjProductListV2ItemDto... items) {
        CjProductListPageDto page = new CjProductListPageDto();
        page.setTotalRecords(total);
        CjProductListV2ContentDto content = new CjProductListV2ContentDto();
        content.setProductList(List.of(items));
        page.setContent(List.of(content));
        return page;
    }

    private static CjProductListV2ItemDto item(String id) {
        CjProductListV2ItemDto i = new CjProductListV2ItemDto();
        i.setId(id);
        i.setNameEn("N");
        i.setSellPrice("10.00");
        return i;
    }

    @Test
    void getStrategy_returnsByCategory() {
        assertThat(strategy.getStrategy()).isEqualTo(DiscoveryStrategy.BY_CATEGORY);
    }

    @Test
    void supportsResume_returnsTrue() {
        assertThat(strategy.supportsResume()).isTrue();
    }

    @Test
    void execute_noCategories_returnsCompleted() {
        when(categoryRepository.findAllLevel3Ids()).thenReturn(List.of());
        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();

        DiscoveryResult result = strategy.execute(state);

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getNewPidsDiscovered()).isZero();
    }

    @Test
    void execute_withCategoriesAndNewPids_savesAndUpdatesState() {
        when(categoryRepository.findAllLevel3Ids()).thenReturn(List.of("cat-1"));
        when(dropshippingPort.getProductListFiltered(eq(1), eq(10), eq("cat-1"), any(), any(), any(), eq(3),
                eq("desc"))).thenReturn(pageWith(1, item("pid-1")));
        when(discoveredPidRepository.existsByPid("pid-1")).thenReturn(false);
        when(productDetailRepository.existsByPid("pid-1")).thenReturn(false);

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);

        assertThat(result.getNewPidsDiscovered()).isEqualTo(1);
        assertThat(result.isCompleted()).isTrue();
        verify(discoveredPidRepository).saveAll(any());
        verify(stateRepository).save(state);
    }

    @Test
    void execute_existingPid_skipsSaveAll() {
        when(categoryRepository.findAllLevel3Ids()).thenReturn(List.of("cat-1"));
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), anyString(), any(), any(), any(), anyInt(),
                anyString())).thenReturn(pageWith(1, item("pid-1")));
        when(discoveredPidRepository.existsByPid("pid-1")).thenReturn(true);

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);

        assertThat(result.getNewPidsDiscovered()).isZero();
        verify(discoveredPidRepository, never()).saveAll(any());
    }

    @Test
    void execute_emptyPageResult_breaks() {
        when(categoryRepository.findAllLevel3Ids()).thenReturn(List.of("cat-1"));
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), anyString(), any(), any(), any(), anyInt(),
                anyString())).thenReturn(pageWith(0));

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);

        assertThat(result.isCompleted()).isTrue();
    }

    @Test
    void execute_blankPid_skipped() {
        when(categoryRepository.findAllLevel3Ids()).thenReturn(List.of("cat-1"));
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), anyString(), any(), any(), any(), anyInt(),
                anyString())).thenReturn(pageWith(1, item(" ")));

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);

        assertThat(result.getNewPidsDiscovered()).isZero();
    }

    @Test
    void execute_exceptionInCategory_continuesToNext() {
        when(categoryRepository.findAllLevel3Ids()).thenReturn(List.of("cat-1", "cat-2"));
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), eq("cat-1"), any(), any(), any(), anyInt(),
                anyString())).thenThrow(new RuntimeException("CJ error"));
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), eq("cat-2"), any(), any(), any(), anyInt(),
                anyString())).thenReturn(pageWith(0));

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);

        assertThat(result.isCompleted()).isTrue();
    }

    @Test
    void execute_resumesFromLastCategory() {
        when(categoryRepository.findAllLevel3Ids()).thenReturn(List.of("cat-1", "cat-2", "cat-3"));
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), eq("cat-3"), any(), any(), any(), anyInt(),
                anyString())).thenReturn(pageWith(0));

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).lastCategoryId("cat-2").build();
        strategy.execute(state);

        verify(dropshippingPort, times(1)).getProductListFiltered(anyInt(), anyInt(), anyString(), any(), any(), any(),
                anyInt(), anyString());
    }
}
