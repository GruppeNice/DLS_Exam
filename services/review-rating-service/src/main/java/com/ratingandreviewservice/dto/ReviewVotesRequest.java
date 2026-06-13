package com.ratingandreviewservice.dto;

import java.util.UUID;

public record ReviewVotesRequest (UUID userId, UUID reviewId, int value){
}
