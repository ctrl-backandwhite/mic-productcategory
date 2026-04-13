package com.backandwhite.domain.model;

import com.backandwhite.domain.valueobject.DiscoveryStatus;
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
public class DiscoveredPid {

    private String id;
    private String pid;
    private String categoryId;
    private String keyword;
    private DiscoveryStrategy strategy;
    private DiscoveryStatus status;
    private String nameEn;
    private String sellPrice;
    private int errorCount;
    private String lastError;
    private Instant discoveredAt;
    private Instant syncedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
