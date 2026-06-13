package com.ratingandreviewservice.dto;

import java.util.UUID;

public record RatingRequest (UUID userId, UUID movieId, int userRating){
}
