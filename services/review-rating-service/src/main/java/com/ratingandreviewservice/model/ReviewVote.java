package com.ratingandreviewservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"review_id", "user_id"})
)
public class ReviewVote {
    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    private int value;
}