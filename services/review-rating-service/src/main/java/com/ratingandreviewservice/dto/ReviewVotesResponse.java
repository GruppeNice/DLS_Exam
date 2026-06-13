package com.ratingandreviewservice.dto;

import java.util.UUID;

public record ReviewVotesResponse(UUID id, UUID userId, UUID reviewId, int value){
}