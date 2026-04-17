package com.backandwhite.domain.model;

import com.backandwhite.domain.valueobject.SyncStatus;
import com.backandwhite.domain.valueobject.SyncType;
import java.time.Instant;
import lombok.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SyncLog {

    private String id;
    private SyncType syncType;
    private SyncStatus status;
    private Instant startedAt;
    private Instant finishedAt;

    @Builder.Default
    private Integer totalItems = 0;

    @Builder.Default
    private Integer syncedItems = 0;

    @Builder.Default
    private Integer failedItems = 0;

    @Builder.Default
    private Integer skippedItems = 0;

    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;
}
