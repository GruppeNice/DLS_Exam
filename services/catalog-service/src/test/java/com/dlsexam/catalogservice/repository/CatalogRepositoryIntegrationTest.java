package com.dlsexam.catalogservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dlsexam.catalogservice.domain.ContentAvailability;
import com.dlsexam.catalogservice.domain.ContentGenreLink;
import com.dlsexam.catalogservice.domain.ContentItem;
import com.dlsexam.catalogservice.domain.ContentRatingStats;
import com.dlsexam.catalogservice.domain.ContentTagLink;
import com.dlsexam.catalogservice.domain.ContentType;
import com.dlsexam.catalogservice.domain.Genre;
import com.dlsexam.catalogservice.domain.Tag;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.type.preferred_uuid_jdbc_type=CHAR"
})
class CatalogRepositoryIntegrationTest {

    @Autowired
    private ContentItemRepository contentItemRepository;
    @Autowired
    private ContentGenreLinkRepository contentGenreLinkRepository;
    @Autowired
    private ContentTagLinkRepository contentTagLinkRepository;
    @Autowired
    private ContentAvailabilityRepository contentAvailabilityRepository;
    @Autowired
    private ContentRatingStatsRepository contentRatingStatsRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private TagRepository tagRepository;

    @Test
    void contentSearch_appliesTitleGenreTagAndRegionFilters() {
        UUID genreId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        saveGenre(genreId, "Action", "action");
        saveTag(tagId, "Popular");

        ContentItem matching = saveContent("Matrix Reloaded", ContentType.MOVIE, false);
        linkGenre(matching.getId(), genreId);
        linkTag(matching.getId(), tagId);
        linkAvailability(matching.getId(), "DK", LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        ContentItem nonMatching = saveContent("Different Show", ContentType.TV_SHOW, false);
        linkAvailability(nonMatching.getId(), "US", LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        Page<ContentItem> result = contentItemRepository.search(
            "matrix",
            ContentType.MOVIE,
            genreId,
            tagId,
            "DK",
            PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(ContentItem::getId).containsExactly(matching.getId());
    }

    @Test
    void ratingStatsRepository_savesAndLoadsByContentId() {
        UUID contentId = UUID.randomUUID();

        ContentRatingStats stats = new ContentRatingStats();
        stats.setContentId(contentId);
        stats.setAverageRating(new BigDecimal("4.25"));
        stats.setRatingCount(12);
        stats.setReviewCount(4);
        stats.setUpdatedAt(Instant.now());
        contentRatingStatsRepository.save(stats);

        ContentRatingStats loaded = contentRatingStatsRepository.findById(contentId).orElseThrow();

        assertThat(loaded.getAverageRating()).isEqualByComparingTo("4.25");
        assertThat(loaded.getRatingCount()).isEqualTo(12);
        assertThat(loaded.getReviewCount()).isEqualTo(4);
    }

    private Genre saveGenre(UUID id, String name, String slug) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        genre.setSlug(slug);
        genre.setCreatedAt(Instant.now());
        return genreRepository.save(genre);
    }

    private Tag saveTag(UUID id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        tag.setCreatedAt(Instant.now());
        return tagRepository.save(tag);
    }

    private ContentItem saveContent(String title, ContentType contentType, boolean deleted) {
        ContentItem item = new ContentItem();
        item.setId(UUID.randomUUID());
        item.setTitle(title);
        item.setContentType(contentType);
        item.setDeleted(deleted);
        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());
        return contentItemRepository.save(item);
    }

    private void linkGenre(UUID contentId, UUID genreId) {
        ContentGenreLink link = new ContentGenreLink();
        link.setContentId(contentId);
        link.setGenreId(genreId);
        contentGenreLinkRepository.save(link);
    }

    private void linkTag(UUID contentId, UUID tagId) {
        ContentTagLink link = new ContentTagLink();
        link.setContentId(contentId);
        link.setTagId(tagId);
        contentTagLinkRepository.save(link);
    }

    private void linkAvailability(UUID contentId, String region, LocalDate from, LocalDate until) {
        ContentAvailability availability = new ContentAvailability();
        availability.setId(UUID.randomUUID());
        availability.setContentId(contentId);
        availability.setRegionCode(region);
        availability.setAvailableFrom(from);
        availability.setAvailableUntil(until);
        availability.setCreatedAt(Instant.now());
        contentAvailabilityRepository.save(availability);
    }
}

