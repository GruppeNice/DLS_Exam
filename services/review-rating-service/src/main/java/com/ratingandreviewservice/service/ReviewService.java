package com.ratingandreviewservice.service;

import com.ratingandreviewservice.dto.RatingRequest;
import com.ratingandreviewservice.dto.ReviewRequest;
import com.ratingandreviewservice.dto.ReviewResponse;
import com.ratingandreviewservice.messaging.ReviewEventPublisher;
import com.ratingandreviewservice.model.Rating;
import com.ratingandreviewservice.model.Review;
import com.ratingandreviewservice.repository.RatingRepository;
import com.ratingandreviewservice.repository.ReviewRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RatingRepository ratingRepository;
    private final RatingService ratingService;
    private final ReviewEventPublisher eventPublisher;

    public ReviewService(
        ReviewRepository reviewRepository,
        RatingRepository ratingRepository,
        RatingService ratingService,
        ReviewEventPublisher eventPublisher
    ) {
        this.reviewRepository = reviewRepository;
        this.ratingRepository = ratingRepository;
        this.ratingService = ratingService;
        this.eventPublisher = eventPublisher;
    }

    public void addReview(ReviewRequest review) {
        Review existing = reviewRepository
            .findByUserIdAndMovieId(review.userId(), review.movieId())
            .orElse(null);

        Review savedReview;
        if (existing != null) {
            existing.setReviewText(review.reviewText());
            existing.setSpoiler(review.spoiler());
            existing.setUpdatedAt(LocalDate.now());
            savedReview = reviewRepository.save(existing);
        } else {
            Review newReview = fromDTO(review);
            newReview.setId(UUID.randomUUID());
            newReview.setCreatedAt(LocalDate.now());
            savedReview = reviewRepository.save(newReview);
        }

        if (savedReview.getId() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save review");
        }

        ratingService.addRating(new RatingRequest(
            review.userId(),
            review.movieId(),
            review.userRating()
        ));

        try {
            eventPublisher.contentReviewed(
                savedReview.getId(),
                savedReview.getUserId(),
                savedReview.getMovieId(),
                savedReview.getReviewText(),
                Boolean.TRUE.equals(savedReview.getSpoiler()),
                Instant.now()
            );
        } catch (RuntimeException ignored) {
            // Review is persisted; event delivery is best-effort for the demo UI.
        }
    }

    public List<ReviewResponse> getReviewsByMovieId(UUID movieId) {
        return reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId).stream()
            .map(this::toDTO)
            .toList();
    }

    public ReviewResponse getReviewById(UUID id) {
        Review review = reviewRepository.findById(id).orElse(null);
        if (review == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review with id " + id + " not found");
        }
        return toDTO(review);
    }

    public ReviewResponse toDTO(Review review) {
        Integer userRating = ratingRepository.findByUserIdAndMovieId(review.getUserId(), review.getMovieId())
            .map(Rating::getUserRating)
            .orElse(null);
        return new ReviewResponse(
            review.getId(),
            review.getUserId(),
            review.getMovieId(),
            review.getReviewText(),
            review.getSpoiler(),
            review.getCreatedAt(),
            userRating
        );
    }

    public Review fromDTO(ReviewRequest reviewDTO) {
        Review review = new Review();
        review.setUserId(reviewDTO.userId());
        review.setMovieId(reviewDTO.movieId());
        review.setReviewText(reviewDTO.reviewText());
        review.setSpoiler(reviewDTO.spoiler());
        return review;
    }
}
