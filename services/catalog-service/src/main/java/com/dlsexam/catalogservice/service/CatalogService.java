package com.dlsexam.catalogservice.service;

import com.dlsexam.catalogservice.domain.CastMember;
import com.dlsexam.catalogservice.domain.ContentAvailability;
import com.dlsexam.catalogservice.domain.ContentGenreLink;
import com.dlsexam.catalogservice.domain.ContentItem;
import com.dlsexam.catalogservice.domain.ContentRatingStats;
import com.dlsexam.catalogservice.domain.ContentTagLink;
import com.dlsexam.catalogservice.domain.ContentType;
import com.dlsexam.catalogservice.domain.Genre;
import com.dlsexam.catalogservice.domain.Tag;
import com.dlsexam.catalogservice.dto.CatalogDtos.AvailabilityInput;
import com.dlsexam.catalogservice.dto.CatalogDtos.CastMemberInput;
import com.dlsexam.catalogservice.dto.CatalogDtos.ContentFilterInput;
import com.dlsexam.catalogservice.dto.CatalogDtos.ContentSearchResult;
import com.dlsexam.catalogservice.dto.CatalogDtos.CreateContentInput;
import com.dlsexam.catalogservice.dto.CatalogDtos.UpdateContentInput;
import com.dlsexam.catalogservice.messaging.CatalogEventPublisher;
import com.dlsexam.catalogservice.repository.CastMemberRepository;
import com.dlsexam.catalogservice.repository.ContentAvailabilityRepository;
import com.dlsexam.catalogservice.repository.ContentGenreLinkRepository;
import com.dlsexam.catalogservice.repository.ContentItemRepository;
import com.dlsexam.catalogservice.repository.ContentRatingStatsRepository;
import com.dlsexam.catalogservice.repository.ContentTagLinkRepository;
import com.dlsexam.catalogservice.repository.GenreRepository;
import com.dlsexam.catalogservice.repository.TagRepository;
import com.dlsexam.catalogservice.security.SecuritySupport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogService {

    private final ContentItemRepository contentItemRepository;
    private final GenreRepository genreRepository;
    private final TagRepository tagRepository;
    private final CastMemberRepository castMemberRepository;
    private final ContentAvailabilityRepository availabilityRepository;
    private final ContentRatingStatsRepository ratingStatsRepository;
    private final ContentGenreLinkRepository genreLinkRepository;
    private final ContentTagLinkRepository tagLinkRepository;
    private final CatalogEventPublisher eventPublisher;

    public CatalogService(
        ContentItemRepository contentItemRepository,
        GenreRepository genreRepository,
        TagRepository tagRepository,
        CastMemberRepository castMemberRepository,
        ContentAvailabilityRepository availabilityRepository,
        ContentRatingStatsRepository ratingStatsRepository,
        ContentGenreLinkRepository genreLinkRepository,
        ContentTagLinkRepository tagLinkRepository,
        CatalogEventPublisher eventPublisher
    ) {
        this.contentItemRepository = contentItemRepository;
        this.genreRepository = genreRepository;
        this.tagRepository = tagRepository;
        this.castMemberRepository = castMemberRepository;
        this.availabilityRepository = availabilityRepository;
        this.ratingStatsRepository = ratingStatsRepository;
        this.genreLinkRepository = genreLinkRepository;
        this.tagLinkRepository = tagLinkRepository;
        this.eventPublisher = eventPublisher;
    }

    public ContentItem getContentById(UUID id) {
        return contentItemRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found"));
    }

    public ContentSearchResult searchContent(ContentFilterInput filter, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        UUID genreId = parseUuid(filter != null ? filter.genreId() : null);
        UUID tagId = parseUuid(filter != null ? filter.tagId() : null);
        ContentType contentType = filter != null ? filter.contentType() : null;
        String titleContains = filter != null ? filter.titleContains() : null;
        String regionCode = filter != null ? filter.regionCode() : null;

        Page<ContentItem> result = contentItemRepository.search(
            blankToNull(titleContains),
            contentType,
            genreId,
            tagId,
            blankToNull(regionCode),
            PageRequest.of(safePage, safeSize)
        );

        return new ContentSearchResult(result.getContent(), (int) result.getTotalElements(), safePage, safeSize);
    }

    public List<Genre> listGenres() {
        return genreRepository.findAll();
    }

    public List<Tag> listTags() {
        return tagRepository.findAll();
    }

    @Transactional
    public ContentItem createContent(CreateContentInput input) {
        SecuritySupport.requireAuthenticatedUser();
        Instant now = Instant.now();
        ContentItem item = new ContentItem();
        item.setId(UUID.randomUUID());
        item.setTitle(input.title());
        item.setDescription(input.description());
        item.setContentType(input.contentType());
        item.setReleaseDate(parseDate(input.releaseDate()));
        item.setDurationMinutes(input.durationMinutes());
        item.setPosterUrl(input.posterUrl());
        item.setDeleted(false);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        contentItemRepository.save(item);

        replaceGenres(item.getId(), input.genreIds());
        replaceTags(item.getId(), input.tagIds());
        saveCast(item.getId(), input.cast(), now);
        saveAvailability(item.getId(), input.availability(), now);

        ContentRatingStats stats = new ContentRatingStats();
        stats.setContentId(item.getId());
        stats.setAverageRating(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        stats.setRatingCount(0);
        stats.setReviewCount(0);
        stats.setUpdatedAt(now);
        ratingStatsRepository.save(stats);

        eventPublisher.contentCreated(item.getId(), item.getTitle(), item.getContentType(), now);
        return item;
    }

    @Transactional
    public ContentItem updateContent(UUID id, UpdateContentInput input) {
        SecuritySupport.requireAuthenticatedUser();
        ContentItem item = getContentById(id);
        if (input.title() != null) {
            item.setTitle(input.title());
        }
        if (input.description() != null) {
            item.setDescription(input.description());
        }
        if (input.contentType() != null) {
            item.setContentType(input.contentType());
        }
        if (input.releaseDate() != null) {
            item.setReleaseDate(parseDate(input.releaseDate()));
        }
        if (input.durationMinutes() != null) {
            item.setDurationMinutes(input.durationMinutes());
        }
        if (input.posterUrl() != null) {
            item.setPosterUrl(input.posterUrl());
        }
        item.setUpdatedAt(Instant.now());
        contentItemRepository.save(item);

        if (input.genreIds() != null) {
            replaceGenres(id, input.genreIds());
        }
        if (input.tagIds() != null) {
            replaceTags(id, input.tagIds());
        }

        eventPublisher.contentUpdated(item.getId(), item.getTitle(), item.getContentType(), item.getUpdatedAt());
        return item;
    }

    @Transactional
    public boolean removeContent(UUID id) {
        SecuritySupport.requireAuthenticatedUser();
        ContentItem item = getContentById(id);
        Instant now = Instant.now();
        item.setDeleted(true);
        item.setDeletedAt(now);
        item.setUpdatedAt(now);
        contentItemRepository.save(item);
        eventPublisher.contentRemoved(item.getId(), now);
        return true;
    }

    @Transactional
    public void applyContentRated(UUID contentId, int stars) {
        if (!contentItemRepository.existsById(contentId)) {
            return;
        }
        ContentRatingStats stats = ratingStatsRepository.findById(contentId).orElseGet(() -> {
            ContentRatingStats created = new ContentRatingStats();
            created.setContentId(contentId);
            created.setAverageRating(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            created.setRatingCount(0);
            created.setReviewCount(0);
            created.setUpdatedAt(Instant.now());
            return created;
        });

        BigDecimal total = stats.getAverageRating().multiply(BigDecimal.valueOf(stats.getRatingCount()));
        int newCount = stats.getRatingCount() + 1;
        BigDecimal newAverage = total.add(BigDecimal.valueOf(stars))
            .divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);
        stats.setAverageRating(newAverage);
        stats.setRatingCount(newCount);
        stats.setUpdatedAt(Instant.now());
        ratingStatsRepository.save(stats);
    }

    @Transactional
    public void applyContentReviewed(UUID contentId) {
        if (!contentItemRepository.existsById(contentId)) {
            return;
        }
        ContentRatingStats stats = ratingStatsRepository.findById(contentId).orElseGet(() -> {
            ContentRatingStats created = new ContentRatingStats();
            created.setContentId(contentId);
            created.setAverageRating(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            created.setRatingCount(0);
            created.setReviewCount(0);
            created.setUpdatedAt(Instant.now());
            return created;
        });
        stats.setReviewCount(stats.getReviewCount() + 1);
        stats.setUpdatedAt(Instant.now());
        ratingStatsRepository.save(stats);
    }

    public List<Genre> genresForContent(ContentItem item) {
        return genreLinkRepository.findByContentId(item.getId()).stream()
            .map(link -> genreRepository.findById(link.getGenreId()).orElse(null))
            .filter(genre -> genre != null)
            .toList();
    }

    public List<Tag> tagsForContent(ContentItem item) {
        return tagLinkRepository.findByContentId(item.getId()).stream()
            .map(link -> tagRepository.findById(link.getTagId()).orElse(null))
            .filter(tag -> tag != null)
            .toList();
    }

    public List<CastMember> castForContent(ContentItem item) {
        return castMemberRepository.findByContentId(item.getId());
    }

    public List<ContentAvailability> availabilityForContent(ContentItem item) {
        return availabilityRepository.findByContentId(item.getId());
    }

    public ContentRatingStats ratingStatsForContent(ContentItem item) {
        return ratingStatsRepository.findById(item.getId()).orElse(null);
    }

    public boolean availableInRegion(ContentItem item, String regionCode) {
        LocalDate today = LocalDate.now();
        return availabilityRepository.findByContentId(item.getId()).stream()
            .filter(rule -> rule.getRegionCode().equalsIgnoreCase(regionCode))
            .anyMatch(rule ->
                (rule.getAvailableFrom() == null || !rule.getAvailableFrom().isAfter(today))
                    && (rule.getAvailableUntil() == null || !rule.getAvailableUntil().isBefore(today))
            );
    }

    private void replaceGenres(UUID contentId, List<String> genreIds) {
        genreLinkRepository.deleteByContentId(contentId);
        if (genreIds == null) {
            return;
        }
        for (String genreIdValue : genreIds) {
            UUID genreId = UUID.fromString(genreIdValue);
            if (!genreRepository.existsById(genreId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown genre: " + genreId);
            }
            ContentGenreLink link = new ContentGenreLink();
            link.setContentId(contentId);
            link.setGenreId(genreId);
            genreLinkRepository.save(link);
        }
    }

    private void replaceTags(UUID contentId, List<String> tagIds) {
        tagLinkRepository.deleteByContentId(contentId);
        if (tagIds == null) {
            return;
        }
        for (String tagIdValue : tagIds) {
            UUID tagId = UUID.fromString(tagIdValue);
            if (!tagRepository.existsById(tagId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown tag: " + tagId);
            }
            ContentTagLink link = new ContentTagLink();
            link.setContentId(contentId);
            link.setTagId(tagId);
            tagLinkRepository.save(link);
        }
    }

    private void saveCast(UUID contentId, List<CastMemberInput> cast, Instant now) {
        if (cast == null) {
            return;
        }
        for (CastMemberInput member : cast) {
            CastMember entity = new CastMember();
            entity.setId(UUID.randomUUID());
            entity.setContentId(contentId);
            entity.setPersonName(member.personName());
            entity.setRoleType(member.roleType());
            entity.setCreatedAt(now);
            castMemberRepository.save(entity);
        }
    }

    private void saveAvailability(UUID contentId, List<AvailabilityInput> availability, Instant now) {
        if (availability == null) {
            return;
        }
        for (AvailabilityInput rule : availability) {
            ContentAvailability entity = new ContentAvailability();
            entity.setId(UUID.randomUUID());
            entity.setContentId(contentId);
            entity.setRegionCode(rule.regionCode());
            entity.setAvailableFrom(parseDate(rule.availableFrom()));
            entity.setAvailableUntil(parseDate(rule.availableUntil()));
            entity.setCreatedAt(now);
            availabilityRepository.save(entity);
        }
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return UUID.fromString(value);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }
}
