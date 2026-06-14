package com.ratingandreviewservice.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ReviewResponse(
    UUID id,
    UUID userId,
    UUID movieId,
    String reviewText,
    Boolean spoiler,
    LocalDate createdAt,
    Integer userRating
) {
}