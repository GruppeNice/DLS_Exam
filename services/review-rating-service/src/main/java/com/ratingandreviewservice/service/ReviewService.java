package com.ratingandreviewservice.service;

import com.ratingandreviewservice.dto.ReviewRequest;
import com.ratingandreviewservice.dto.ReviewResponse;
import com.ratingandreviewservice.model.Review;
import com.ratingandreviewservice.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public void addReview(ReviewRequest review){
        Review newReview = fromDTO(review);
        newReview.setId(UUID.randomUUID());
        Review savedReview = reviewRepository.save(newReview);
        if(savedReview.getId()==null){
            throw new IllegalArgumentException("Failed to save review");
        }
    }

    public ReviewResponse getReviewById(UUID id){
        Review review = reviewRepository.findById(id).orElse(null);
        if(review == null){
            throw new IllegalArgumentException("Review with id " + id + " not found");
        }
        return toDTO(review);
    }

    public ReviewResponse toDTO(Review review){
        return new ReviewResponse(review.getId(), review.getUserId(), review.getMovieId() , review.getReviewText(), review.getSpoiler());
    }

    public Review fromDTO(ReviewRequest reviewDTO){
        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setUserId(reviewDTO.userId());
        review.setMovieId(reviewDTO.movieId());
        review.setReviewText(reviewDTO.reviewText());
        review.setSpoiler(reviewDTO.spoiler());
        return review;
    }

}
