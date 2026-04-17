package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.DiscoveryStateStatus;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryStatusDtoOut {
    private DiscoveryStrategy strategy;
    private DiscoveryStateStatus status;
    private int totalDiscovered;
    private Instant lastRunAt;
    private Instant lastCrawledAt;
    private String lastCategoryId;
    private String lastKeyword;
}
