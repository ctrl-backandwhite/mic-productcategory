package com.backandwhite.application.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiscoveryByTimeStrategyTest {

    @Mock
    private DropshippingPort dropshippingPort;
    @Mock
    private DiscoveredPidRepository discoveredPidRepository;
    @Mock
    private ProductDetailRepository productDetailRepository;
    @Mock
    private DiscoveryStateRepository stateRepository;

    private CjDropshippingProperties properties;
    private DiscoveryByTimeStrategy strategy;

    @BeforeEach
    void setUp() {
        properties = new CjDropshippingProperties();
        properties.getDiscovery().setPageSize(10);
        properties.getDiscovery().setRateLimitWaitMs(0L);
        strategy = new DiscoveryByTimeStrategy(dropshippingPort, discoveredPidRepository, productDetailRepository,
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
    void getStrategy_returnsByTime() {
        assertThat(strategy.getStrategy()).isEqualTo(DiscoveryStrategy.BY_TIME);
    }

    @Test
    void supportsResume_returnsTrue() {
        assertThat(strategy.supportsResume()).isTrue();
    }

    @Test
    void execute_firstRun_usesDefaultWindow_savesNewPid() {
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), any(), any(), anyLong(), anyLong(), anyInt(),
                anyString())).thenReturn(pageWith(1, item("pid-1")));
        when(discoveredPidRepository.existsByPid("pid-1")).thenReturn(false);
        when(productDetailRepository.existsByPid("pid-1")).thenReturn(false);

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);

        assertThat(result.getNewPidsDiscovered()).isEqualTo(1);
        verify(discoveredPidRepository).saveAll(any());
        verify(stateRepository).save(state);
    }

    @Test
    void execute_resumedRun_usesLastCrawledAt() {
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), any(), any(), anyLong(), anyLong(), anyInt(),
                anyString())).thenReturn(pageWith(0));

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).lastCrawledAt(Instant.now()).build();
        DiscoveryResult result = strategy.execute(state);

        assertThat(result.isCompleted()).isTrue();
    }

    @Test
    void execute_nullResult_breaks() {
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), any(), any(), anyLong(), anyLong(), anyInt(),
                anyString())).thenReturn(null);

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);

        assertThat(result.isCompleted()).isTrue();
    }

    @Test
    void execute_blankPid_skipped() {
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), any(), any(), anyLong(), anyLong(), anyInt(),
                anyString())).thenReturn(pageWith(1, item(" ")));

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);
        assertThat(result.getNewPidsDiscovered()).isZero();
    }

    @Test
    void execute_existingPid_skipsSave() {
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), any(), any(), anyLong(), anyLong(), anyInt(),
                anyString())).thenReturn(pageWith(1, item("pid-1")));
        when(discoveredPidRepository.existsByPid("pid-1")).thenReturn(true);

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);

        assertThat(result.getNewPidsDiscovered()).isZero();
        verify(discoveredPidRepository, never()).saveAll(any());
    }

    @Test
    void execute_exception_breaks() {
        when(dropshippingPort.getProductListFiltered(anyInt(), anyInt(), any(), any(), anyLong(), anyLong(), anyInt(),
                anyString())).thenThrow(new RuntimeException("CJ"));

        DiscoveryState state = DiscoveryState.builder().totalDiscovered(0).build();
        DiscoveryResult result = strategy.execute(state);
        assertThat(result.isCompleted()).isTrue();
    }
}
