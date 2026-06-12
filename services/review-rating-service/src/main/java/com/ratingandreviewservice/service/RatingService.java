package com.ratingandreviewservice.service;

import com.ratingandreviewservice.model.Rating;
import com.ratingandreviewservice.repository.RatingRepository;
import org.springframework.stereotype.Service;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public Rating addReview(Rating rating){
        return ratingRepository.save(rating);
    }

    public Rating getReviewById(Integer id){
        return ratingRepository.findById(id).orElse(null);
    }

}
