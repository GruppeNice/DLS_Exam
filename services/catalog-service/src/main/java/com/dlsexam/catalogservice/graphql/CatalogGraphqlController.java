package com.dlsexam.catalogservice.graphql;

import com.dlsexam.catalogservice.domain.CastMember;
import com.dlsexam.catalogservice.domain.ContentAvailability;
import com.dlsexam.catalogservice.domain.ContentItem;
import com.dlsexam.catalogservice.domain.ContentRatingStats;
import com.dlsexam.catalogservice.domain.Genre;
import com.dlsexam.catalogservice.domain.Tag;
import com.dlsexam.catalogservice.dto.CatalogDtos.ContentFilterInput;
import com.dlsexam.catalogservice.dto.CatalogDtos.ContentSearchResult;
import com.dlsexam.catalogservice.dto.CatalogDtos.CreateContentInput;
import com.dlsexam.catalogservice.dto.CatalogDtos.UpdateContentInput;
import com.dlsexam.catalogservice.service.CatalogService;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class CatalogGraphqlController {

    private final CatalogService catalogService;

    public CatalogGraphqlController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @QueryMapping
    public ContentItem contentById(@Argument UUID id) {
        return catalogService.getContentById(id);
    }

    @QueryMapping
    public ContentSearchResult searchContent(
        @Argument ContentFilterInput filter,
        @Argument Integer page,
        @Argument Integer size
    ) {
        return catalogService.searchContent(filter, page == null ? 0 : page, size == null ? 20 : size);
    }

    @QueryMapping
    public java.util.List<Genre> genres() {
        return catalogService.listGenres();
    }

    @QueryMapping
    public java.util.List<Tag> tags() {
        return catalogService.listTags();
    }

    @MutationMapping
    public ContentItem createContent(@Argument CreateContentInput input) {
        return catalogService.createContent(input);
    }

    @MutationMapping
    public ContentItem updateContent(@Argument UUID id, @Argument UpdateContentInput input) {
        return catalogService.updateContent(id, input);
    }

    @MutationMapping
    public boolean removeContent(@Argument UUID id) {
        return catalogService.removeContent(id);
    }

    @SchemaMapping(typeName = "ContentItem")
    public java.util.List<Genre> genres(ContentItem item) {
        return catalogService.genresForContent(item);
    }

    @SchemaMapping(typeName = "ContentItem")
    public java.util.List<Tag> tags(ContentItem item) {
        return catalogService.tagsForContent(item);
    }

    @SchemaMapping(typeName = "ContentItem")
    public java.util.List<CastMember> cast(ContentItem item) {
        return catalogService.castForContent(item);
    }

    @SchemaMapping(typeName = "ContentItem")
    public java.util.List<ContentAvailability> availability(ContentItem item) {
        return catalogService.availabilityForContent(item);
    }

    @SchemaMapping(typeName = "ContentItem")
    public ContentRatingStats ratingStats(ContentItem item) {
        return catalogService.ratingStatsForContent(item);
    }

    @SchemaMapping(typeName = "ContentItem")
    public boolean availableInRegion(ContentItem item, @Argument String regionCode) {
        return catalogService.availableInRegion(item, regionCode);
    }

    @SchemaMapping(typeName = "ContentItem")
    public String releaseDate(ContentItem item) {
        return item.getReleaseDate() != null ? item.getReleaseDate().toString() : null;
    }

    @SchemaMapping(typeName = "Availability")
    public String availableFrom(ContentAvailability availability) {
        return availability.getAvailableFrom() != null ? availability.getAvailableFrom().toString() : null;
    }

    @SchemaMapping(typeName = "Availability")
    public String availableUntil(ContentAvailability availability) {
        return availability.getAvailableUntil() != null ? availability.getAvailableUntil().toString() : null;
    }
}
