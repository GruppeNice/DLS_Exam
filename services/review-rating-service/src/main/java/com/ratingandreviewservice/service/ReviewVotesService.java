package com.ratingandreviewservice.service;

import com.ratingandreviewservice.dto.ReviewVotesRequest;
import com.ratingandreviewservice.dto.ReviewVotesResponse;
import com.ratingandreviewservice.messaging.ReviewEventPublisher;
import com.ratingandreviewservice.model.Review;
import com.ratingandreviewservice.model.ReviewVote;
import com.ratingandreviewservice.repository.ReviewRepository;
import com.ratingandreviewservice.repository.ReviewVotesRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReviewVotesService {

    private final ReviewVotesRepository reviewVotesRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewEventPublisher eventPublisher;

    public ReviewVotesService(
        ReviewVotesRepository reviewVotesRepository,
        ReviewRepository reviewRepository,
        ReviewEventPublisher eventPublisher
    ) {
        this.reviewVotesRepository = reviewVotesRepository;
        this.reviewRepository = reviewRepository;
        this.eventPublisher = eventPublisher;
    }

    public void addReviewVote(ReviewVotesRequest reviewVotesRequest) {
        if (reviewVotesRequest.value() != 1 && reviewVotesRequest.value() != -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vote value must be 1 (upvote) or -1 (downvote)");
        }

        Review review = reviewRepository.findById(reviewVotesRequest.reviewId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review with id " + reviewVotesRequest.reviewId() + " not found")
        );

        if (review.getUserId().equals(reviewVotesRequest.userId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot vote on your own review");
        }

        ReviewVote savedReviewVote = reviewVotesRepository
            .findByReviewAndUserId(review, reviewVotesRequest.userId())
            .map(existing -> {
                if (existing.getValue() == reviewVotesRequest.value()) {
                    return existing;
                }
                existing.setValue(reviewVotesRequest.value());
                return reviewVotesRepository.save(existing);
            })
            .orElseGet(() -> {
                ReviewVote reviewVote = new ReviewVote();
                reviewVote.setId(UUID.randomUUID());
                reviewVote.setReview(review);
                reviewVote.setUserId(reviewVotesRequest.userId());
                reviewVote.setValue(reviewVotesRequest.value());
                return reviewVotesRepository.save(reviewVote);
            });

        try {
            eventPublisher.reviewVoted(
                savedReviewVote.getReview().getId(),
                savedReviewVote.getUserId(),
                review.getUserId(),
                review.getReviewText() == null ? "" : review.getReviewText(),
                savedReviewVote.getValue(),
                Instant.now()
            );
        } catch (RuntimeException ignored) {
            // Vote is persisted; event delivery is best-effort for the demo UI.
        }
    }

    public List<ReviewVotesResponse> getReviewVotesByReviewId(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review with id " + reviewId + " not found")
        );
        return reviewVotesRepository.findReviewVoteByReview(review);
    }
}
