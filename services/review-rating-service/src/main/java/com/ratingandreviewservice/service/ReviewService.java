package com.ratingandreviewservice.service;

import com.ratingandreviewservice.model.Review;
import com.ratingandreviewservice.repository.ReviewRepository;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review addReview(Review review){
        return reviewRepository.save(review);
    }

    public Review getReviewById(Integer id){
        return reviewRepository.findById(id).orElse(null);
    }
}
