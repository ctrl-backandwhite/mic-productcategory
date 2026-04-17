package com.backandwhite.infrastructure.db.postgres.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "review_helpful_votes", uniqueConstraints = {
        @UniqueConstraint(name = "uq_review_helpful_vote", columnNames = {"review_id", "session_id"})})
public class ReviewHelpfulVoteEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "review_id", length = 64, nullable = false)
    private String reviewId;

    @Column(name = "session_id", length = 255, nullable = false)
    private String sessionId;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
