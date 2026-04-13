package com.backandwhite.infrastructure.db.postgres.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "discovered_pids")
public class DiscoveredPidEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "pid", length = 200, nullable = false, unique = true)
    private String pid;

    @Column(name = "category_id", length = 200)
    private String categoryId;

    @Column(name = "keyword", length = 200)
    private String keyword;

    @Column(name = "strategy", length = 20, nullable = false)
    private String strategy;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "name_en", length = 500)
    private String nameEn;

    @Column(name = "sell_price", length = 50)
    private String sellPrice;

    @Builder.Default
    @Column(name = "error_count", nullable = false)
    private Integer errorCount = 0;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "discovered_at", nullable = false)
    private Instant discoveredAt;

    @Column(name = "synced_at")
    private Instant syncedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
