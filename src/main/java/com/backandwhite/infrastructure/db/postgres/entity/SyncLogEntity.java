package com.backandwhite.infrastructure.db.postgres.entity;

import com.backandwhite.common.infrastructure.entity.AuditableEntity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import lombok.experimental.SuperBuilder;

@With
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sync_log", indexes = {@Index(name = "idx_sync_log_type_status", columnList = "sync_type, status"),
        @Index(name = "idx_sync_log_started", columnList = "started_at")})
public class SyncLogEntity extends AuditableEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "sync_type", length = 30, nullable = false)
    private String syncType;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "RUNNING";

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "total_items", nullable = false)
    @Builder.Default
    private Integer totalItems = 0;

    @Column(name = "synced_items", nullable = false)
    @Builder.Default
    private Integer syncedItems = 0;

    @Column(name = "failed_items", nullable = false)
    @Builder.Default
    private Integer failedItems = 0;

    @Column(name = "skipped_items", nullable = false)
    @Builder.Default
    private Integer skippedItems = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
