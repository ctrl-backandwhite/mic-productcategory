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
@Table(name = "discovery_state")
public class DiscoveryStateEntity {

    @Id
    @Column(name = "strategy", length = 30)
    private String strategy;

    @Column(name = "last_crawled_at")
    private Instant lastCrawledAt;

    @Column(name = "last_category_id", length = 200)
    private String lastCategoryId;

    @Column(name = "last_keyword", length = 200)
    private String lastKeyword;

    @Builder.Default
    @Column(name = "last_page")
    private Integer lastPage = 0;

    @Builder.Default
    @Column(name = "total_discovered")
    private Integer totalDiscovered = 0;

    @Column(name = "last_run_at")
    private Instant lastRunAt;

    @Builder.Default
    @Column(name = "status", length = 20)
    private String status = "IDLE";

    @Column(name = "updated_at")
    private Instant updatedAt;
}
