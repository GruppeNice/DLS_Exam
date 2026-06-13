package com.ratingandreviewservice.service;

import com.ratingandreviewservice.dto.ReviewRequest;
import com.ratingandreviewservice.dto.ReviewResponse;
import com.ratingandreviewservice.messaging.ReviewEventPublisher;
import com.ratingandreviewservice.model.Review;
import com.ratingandreviewservice.repository.ReviewRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewEventPublisher eventPublisher;

    public ReviewService(ReviewRepository reviewRepository, ReviewEventPublisher eventPublisher) {
        this.reviewRepository = reviewRepository;
        this.eventPublisher = eventPublisher;
    }

    public void addReview(ReviewRequest review) {
        Review newReview = fromDTO(review);
        newReview.setId(UUID.randomUUID());
        newReview.setCreatedAt(LocalDate.now());
        Review savedReview = reviewRepository.save(newReview);
        if (savedReview.getId() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save review");
        }
        eventPublisher.contentReviewed(
            savedReview.getId(),
            savedReview.getUserId(),
            savedReview.getMovieId(),
            Instant.now()
        );
    }

    public ReviewResponse getReviewById(UUID id) {
        Review review = reviewRepository.findById(id).orElse(null);
        if (review == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review with id " + id + " not found");
        }
        return toDTO(review);
    }

    public ReviewResponse toDTO(Review review) {
        return new ReviewResponse(review.getId(), review.getUserId(), review.getMovieId(), review.getReviewText(), review.getSpoiler());
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
