package com.backandwhite.infrastructure.db.postgres.entity;

import com.backandwhite.common.infrastructure.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@With
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sync_failure", indexes = {
        @Index(name = "idx_sync_failure_log", columnList = "sync_log_id")
})
public class SyncFailureEntity extends AuditableEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "sync_log_id", length = 64)
    private String syncLogId;

    @Column(name = "entity_type", length = 30, nullable = false)
    private String entityType;

    @Column(name = "entity_id", length = 128, nullable = false)
    private String entityId;

    @Column(name = "error_code", length = 20)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retries", nullable = false)
    @Builder.Default
    private Integer maxRetries = 3;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "resolved", nullable = false)
    @Builder.Default
    private Boolean resolved = false;
}
