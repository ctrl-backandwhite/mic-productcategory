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
class DiscoveryByKeywordStrategyTest {

    @Mock
    private DropshippingPort dropshippingPort;
    @Mock
    private DiscoveredPidRepository discoveredPidRepository;
    @Mock
    private ProductDetailRepository productDetailRepository;
    @Mock
    private DiscoveryStateRepository stateRepository;

    private CjDropshippingProperties properties;
    private DiscoveryByKeywordStrategy strategy;

    @BeforeEach
    void setUp() {
        properties = new CjDropshippingProperties();
        properties.getDiscovery().setMaxPagesPerKeyword(1);
        properties.getDiscovery().setPageSize(10);
        properties.getDiscovery().setRateLimitWaitMs(0L);
        strategy = new DiscoveryByKeywordStrategy(dropshippingPort, discoveredPidRepository, productDetailRepository,
                stateRepository, properties);
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
        return i;
    }

    @Test
    void getStrategy_returnsByKeyword() {
        assertThat(strategy.getStrategy()).isEqualTo(DiscoveryStrategy.BY_KEYWORD);
    }

    @Test
    void supportsResume_returnsTrue() {
        assertThat(strategy.supportsResume()).isTrue();
    }

    @Test
    void execute_noKeywords_returnsCompleted() {
        properties.getDiscovery().setKeywords(List.of());
        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);
        assertThat(result.isCompleted()).isTrue();
    }

    @Test
    void execute_nullKeywords_returnsCompleted() {
        properties.getDiscovery().setKeywords(null);
        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);
        assertThat(result.isCompleted()).isTrue();
    }

    @Test
    void execute_withKeywordsAndNewPid_savesAndUpdates() {
        properties.getDiscovery().setKeywords(List.of("k1"));
        when(dropshippingPort.getProductListFiltered(eq(1), eq(10), any(), eq("k1"), any(), any(), eq(3), eq("desc")))
                .thenReturn(pageWith(1, item("pid-1")));
        when(discoveredPidRepository.existsByPid("pid-1")).thenReturn(false);
        when(productDetailRepository.existsByPid("pid-1")).thenReturn(false);

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);

        assertThat(result.getNewPidsDiscovered()).isEqualTo(1);
        verify(discoveredPidRepository).saveAll(any());
        verify(stateRepository).save(state);
    }

    @Test
    void execute_existingPid_skipsSaveAll() {
        properties.getDiscovery().setKeywords(List.of("k1"));
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), any(), anyString(), any(), any(), anyInt(),
                anyString())).thenReturn(pageWith(1, item("pid-1")));
        when(discoveredPidRepository.existsByPid("pid-1")).thenReturn(true);

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);

        assertThat(result.getNewPidsDiscovered()).isZero();
        verify(discoveredPidRepository, never()).saveAll(any());
    }

    @Test
    void execute_blankPid_skipped() {
        properties.getDiscovery().setKeywords(List.of("k1"));
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), any(), anyString(), any(), any(), anyInt(),
                anyString())).thenReturn(pageWith(1, item(" ")));

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);

        assertThat(result.getNewPidsDiscovered()).isZero();
    }

    @Test
    void execute_emptyResult_breaks() {
        properties.getDiscovery().setKeywords(List.of("k1"));
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), any(), anyString(), any(), any(), anyInt(),
                anyString())).thenReturn(pageWith(0));
        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);
        assertThat(result.isCompleted()).isTrue();
    }

    @Test
    void execute_exceptionInKeyword_continues() {
        properties.getDiscovery().setKeywords(List.of("k1", "k2"));
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), any(), eq("k1"), any(), any(), anyInt(),
                anyString())).thenThrow(new RuntimeException("CJ"));
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), any(), eq("k2"), any(), any(), anyInt(),
                anyString())).thenReturn(pageWith(0));

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);
        assertThat(result.isCompleted()).isTrue();
    }

    @Test
    void execute_resumesFromLastKeyword() {
        properties.getDiscovery().setKeywords(List.of("k1", "k2", "k3"));
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), any(), eq("k3"), any(), any(), anyInt(),
                anyString())).thenReturn(pageWith(0));

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).lastKeyword("k2").build();
        strategy.execute(state);
        verify(dropshippingPort, times(1)).getProductListFiltered(anyInt(), anyInt(), any(), anyString(), any(), any(),
                anyInt(), anyString());
    }
}
