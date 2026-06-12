package com.ratingandreviewservice.service;

import com.ratingandreviewservice.repository.ReviewRepository;
import com.ratingandreviewservice.repository.ReviewVotesRepository;
import org.springframework.stereotype.Service;

@Service
public class ReviewVotesService {

    private final ReviewVotesRepository reviewVotesRepository;
    private final ReviewRepository reviewRepository;

    public ReviewVotesService(ReviewVotesRepository reviewVotesRepository, ReviewRepository reviewRepository) {
        this.reviewVotesRepository = reviewVotesRepository;
        this.reviewRepository = reviewRepository;
    }

}
