package com.ratingandreviewservice.dto;

import java.util.UUID;

public record RatingResponse(UUID id, UUID userId, UUID movieId, int userRating) {
}