package com.backandwhite.api.dto.out;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CjSyncResultDtoOut {

    private int totalItems;
    private int syncedItems;
    private int failedItems;
    private int skippedItems;
    private long durationMs;
    private String syncLogId;
}
