package com.ratingandreviewservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReviewRequest(
    @NotNull UUID userId,
    @NotNull UUID movieId,
    @NotBlank String reviewText,
    Boolean spoiler,
    @NotNull @Min(1) @Max(5) Integer userRating
) {
}
