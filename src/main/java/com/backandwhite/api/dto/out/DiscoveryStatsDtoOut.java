package com.backandwhite.api.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryStatsDtoOut {
    private long totalDiscovered;
    private long statusNew;
    private long statusQueued;
    private long statusSynced;
    private long statusFailed;
    private long statusSkipped;
    private long byCategory;
    private long byKeyword;
    private long byTime;
}
