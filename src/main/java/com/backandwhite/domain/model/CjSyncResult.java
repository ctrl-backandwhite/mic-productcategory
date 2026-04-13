package com.backandwhite.domain.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CjSyncResult {

    private int totalItems;
    private int syncedItems;
    private int failedItems;
    private int skippedItems;
    private long durationMs;
    private String syncLogId;
}
