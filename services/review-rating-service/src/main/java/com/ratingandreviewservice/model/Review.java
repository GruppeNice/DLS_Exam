package com.ratingandreviewservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Review {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private UUID userId;

    @Column(unique = true, nullable = false)
    private UUID movieId;

    private String reviewText;

    private Boolean spoiler;

    private LocalDate createdAt;

    private LocalDate updatedAt;

}
