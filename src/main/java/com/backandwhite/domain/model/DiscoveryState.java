package com.backandwhite.domain.model;

import com.backandwhite.domain.valueobject.DiscoveryStateStatus;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.time.Instant;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryState {

    private DiscoveryStrategy strategy;
    private Instant lastCrawledAt;
    private String lastCategoryId;
    private String lastKeyword;
    private int lastPage;
    private int totalDiscovered;
    private Instant lastRunAt;
    private DiscoveryStateStatus status;
    private Instant updatedAt;
}
