package com.ratingandreviewservice.dto;

import java.util.UUID;

public record ReviewRequest (UUID userId, UUID movieId, String reviewText, Boolean spoiler){
}
