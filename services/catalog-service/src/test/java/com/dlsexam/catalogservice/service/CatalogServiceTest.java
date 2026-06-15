package com.dlsexam.catalogservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dlsexam.catalogservice.domain.CastRoleType;
import com.dlsexam.catalogservice.domain.ContentItem;
import com.dlsexam.catalogservice.domain.ContentRatingStats;
import com.dlsexam.catalogservice.domain.ContentType;
import com.dlsexam.catalogservice.dto.CatalogDtos.AvailabilityInput;
import com.dlsexam.catalogservice.dto.CatalogDtos.CastMemberInput;
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
import com.dlsexam.catalogservice.security.UserPrincipal;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ContentItemRepository contentItemRepository;
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private CastMemberRepository castMemberRepository;
    @Mock
    private ContentAvailabilityRepository availabilityRepository;
    @Mock
    private ContentRatingStatsRepository ratingStatsRepository;
    @Mock
    private ContentGenreLinkRepository genreLinkRepository;
    @Mock
    private ContentTagLinkRepository tagLinkRepository;
    @Mock
    private CatalogEventPublisher eventPublisher;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createContent_createsEntityLinksAndStats_thenPublishesEvent() {
        setAuthenticatedUser();
        CatalogService service = buildService();

        UUID genreId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        when(genreRepository.existsById(genreId)).thenReturn(true);
        when(tagRepository.existsById(tagId)).thenReturn(true);
        when(contentItemRepository.save(any(ContentItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ratingStatsRepository.save(any(ContentRatingStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateContentInput input = new CreateContentInput(
            "The Example Movie",
            "A test description",
            ContentType.MOVIE,
            "2025-01-01",
            120,
            "https://example.local/poster.jpg",
            List.of(genreId.toString()),
            List.of(tagId.toString()),
            List.of(new CastMemberInput("Ada Actor", CastRoleType.ACTOR)),
            List.of(new AvailabilityInput("DK", "2025-01-01", "2025-12-31"))
        );

        ContentItem created = service.createContent(input);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo("The Example Movie");
        assertThat(created.getContentType()).isEqualTo(ContentType.MOVIE);
        verify(contentItemRepository).save(any(ContentItem.class));
        verify(genreLinkRepository).save(any());
        verify(tagLinkRepository).save(any());
        verify(castMemberRepository).save(any());
        verify(availabilityRepository).save(any());
        verify(ratingStatsRepository).save(any(ContentRatingStats.class));
        verify(eventPublisher).contentCreated(any(), anyString(), any(), any(Instant.class));
    }

    @Test
    void updateContent_updatesSelectedFieldsAndPublishesEvent() {
        setAuthenticatedUser();
        CatalogService service = buildService();

        UUID contentId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        ContentItem existing = new ContentItem();
        existing.setId(contentId);
        existing.setTitle("Old title");
        existing.setContentType(ContentType.MOVIE);
        existing.setDeleted(false);

        when(contentItemRepository.findByIdAndDeletedFalse(contentId)).thenReturn(Optional.of(existing));
        when(contentItemRepository.save(any(ContentItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(genreRepository.existsById(genreId)).thenReturn(true);
        when(tagRepository.existsById(tagId)).thenReturn(true);

        UpdateContentInput input = new UpdateContentInput(
            "Updated title",
            "Updated description",
            ContentType.TV_SHOW,
            "2025-05-01",
            48,
            "https://example.local/new-poster.jpg",
            List.of(genreId.toString()),
            List.of(tagId.toString())
        );

        ContentItem updated = service.updateContent(contentId, input);

        assertThat(updated.getTitle()).isEqualTo("Updated title");
        assertThat(updated.getContentType()).isEqualTo(ContentType.TV_SHOW);
        verify(genreLinkRepository).deleteByContentId(contentId);
        verify(tagLinkRepository).deleteByContentId(contentId);
        verify(genreLinkRepository).save(any());
        verify(tagLinkRepository).save(any());
        verify(eventPublisher).contentUpdated(any(), anyString(), any(), any(Instant.class));
    }

    @Test
    void applyContentRated_recalculatesAverageAndCount() {
        CatalogService service = buildService();
        UUID contentId = UUID.randomUUID();

        ContentRatingStats stats = new ContentRatingStats();
        stats.setContentId(contentId);
        stats.setAverageRating(new BigDecimal("4.50"));
        stats.setRatingCount(2);
        stats.setReviewCount(1);
        stats.setUpdatedAt(Instant.now());

        when(contentItemRepository.existsById(contentId)).thenReturn(true);
        when(ratingStatsRepository.findById(contentId)).thenReturn(Optional.of(stats));

        service.applyContentRated(contentId, 5);

        assertThat(stats.getRatingCount()).isEqualTo(3);
        assertThat(stats.getAverageRating()).isEqualByComparingTo("4.67");
        verify(ratingStatsRepository, atLeastOnce()).save(stats);
    }

    private CatalogService buildService() {
        return new CatalogService(
            contentItemRepository,
            genreRepository,
            tagRepository,
            castMemberRepository,
            availabilityRepository,
            ratingStatsRepository,
            genreLinkRepository,
            tagLinkRepository,
            eventPublisher
        );
    }

    private void setAuthenticatedUser() {
        UserPrincipal principal = new UserPrincipal(UUID.randomUUID(), "tester@catalog.local", Set.of("ADMIN"));
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

