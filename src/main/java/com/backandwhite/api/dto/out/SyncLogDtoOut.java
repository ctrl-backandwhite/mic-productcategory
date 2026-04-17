package com.backandwhite.api.dto.out;

import java.time.Instant;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncLogDtoOut {

    private String id;
    private String syncType;
    private String status;
    private Instant startedAt;
    private Instant finishedAt;
    private Integer totalItems;
    private Integer syncedItems;
    private Integer failedItems;
    private Integer skippedItems;
    private String errorMessage;
}
