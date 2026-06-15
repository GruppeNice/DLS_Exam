package com.ratingandreviewservice.service;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ratingandreviewservice.dto.ReviewVotesRequest;
import com.ratingandreviewservice.messaging.ReviewEventPublisher;
import com.ratingandreviewservice.model.Review;
import com.ratingandreviewservice.model.ReviewVote;
import com.ratingandreviewservice.repository.ReviewRepository;
import com.ratingandreviewservice.repository.ReviewVotesRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewVotesServiceTest {

    @Mock
    private ReviewVotesRepository reviewVotesRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewEventPublisher reviewEventPublisher;

    private ReviewVotesService reviewVotesService;

    @BeforeEach
    void setUp() {
        reviewVotesService = new ReviewVotesService(reviewVotesRepository, reviewRepository, reviewEventPublisher);
    }

    @Test
    void addReviewVotePublishesEventForUpvote() {
        UUID reviewId = UUID.randomUUID();
        UUID voterId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Review review = new Review();
        review.setId(reviewId);
        review.setUserId(authorId);
        review.setReviewText("Solid watch");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewVotesRepository.findByReviewAndUserId(review, voterId)).thenReturn(Optional.empty());
        when(reviewVotesRepository.save(any(ReviewVote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reviewVotesService.addReviewVote(new ReviewVotesRequest(voterId, reviewId, 1));

        ArgumentCaptor<UUID> authorCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Integer> valueCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(reviewEventPublisher).reviewVoted(
            any(UUID.class),
            any(UUID.class),
            authorCaptor.capture(),
            any(String.class),
            valueCaptor.capture(),
            any()
        );
        assertEquals(authorId, authorCaptor.getValue());
        assertEquals(1, valueCaptor.getValue());
    }

    @Test
    void addReviewVotePublishesEventForDownvote() {
        UUID reviewId = UUID.randomUUID();
        UUID voterId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Review review = new Review();
        review.setId(reviewId);
        review.setUserId(authorId);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewVotesRepository.findByReviewAndUserId(review, voterId)).thenReturn(Optional.empty());
        when(reviewVotesRepository.save(any(ReviewVote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reviewVotesService.addReviewVote(new ReviewVotesRequest(voterId, reviewId, -1));

        ArgumentCaptor<Integer> valueCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(reviewEventPublisher).reviewVoted(
            any(UUID.class),
            any(UUID.class),
            any(UUID.class),
            any(String.class),
            valueCaptor.capture(),
            any()
        );
        assertEquals(-1, valueCaptor.getValue());
    }
}
