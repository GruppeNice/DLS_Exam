package com.dlsexam.catalogservice.dto;

import com.dlsexam.catalogservice.domain.CastRoleType;
import com.dlsexam.catalogservice.domain.ContentType;
import java.util.List;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record ContentFilterInput(
        String titleContains,
        String genreId,
        String tagId,
        ContentType contentType,
        String regionCode
    ) {
    }

    public record CreateContentInput(
        String title,
        String description,
        ContentType contentType,
        String releaseDate,
        Integer durationMinutes,
        String posterUrl,
        List<String> genreIds,
        List<String> tagIds,
        List<CastMemberInput> cast,
        List<AvailabilityInput> availability
    ) {
    }

    public record UpdateContentInput(
        String title,
        String description,
        ContentType contentType,
        String releaseDate,
        Integer durationMinutes,
        String posterUrl,
        List<String> genreIds,
        List<String> tagIds
    ) {
    }

    public record CastMemberInput(
        String personName,
        CastRoleType roleType
    ) {
    }

    public record AvailabilityInput(
        String regionCode,
        String availableFrom,
        String availableUntil
    ) {
    }

    public record ContentSearchResult(
        List<com.dlsexam.catalogservice.domain.ContentItem> items,
        int totalCount,
        int page,
        int size
    ) {
    }
}
