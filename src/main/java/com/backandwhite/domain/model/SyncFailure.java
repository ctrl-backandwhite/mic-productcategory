package com.backandwhite.domain.model;

import lombok.*;

import java.time.Instant;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SyncFailure {

    private String id;
    private String syncLogId;
    private String entityType;
    private String entityId;
    private String errorCode;
    private String errorMessage;

    @Builder.Default
    private Integer retryCount = 0;

    @Builder.Default
    private Integer maxRetries = 3;

    private Instant nextRetryAt;

    @Builder.Default
    private Boolean resolved = false;

    private Instant createdAt;
    private Instant updatedAt;
}
