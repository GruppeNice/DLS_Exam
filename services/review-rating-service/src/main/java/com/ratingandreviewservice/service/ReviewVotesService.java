package com.ratingandreviewservice.service;

import com.ratingandreviewservice.dto.ReviewVotesRequest;
import com.ratingandreviewservice.dto.ReviewVotesResponse;
import com.ratingandreviewservice.model.Review;
import com.ratingandreviewservice.model.ReviewVote;
import com.ratingandreviewservice.repository.ReviewRepository;
import com.ratingandreviewservice.repository.ReviewVotesRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReviewVotesService {

    private final ReviewVotesRepository reviewVotesRepository;
    private final ReviewRepository reviewRepository;

    public ReviewVotesService(ReviewVotesRepository reviewVotesRepository, ReviewRepository reviewRepository) {
        this.reviewVotesRepository = reviewVotesRepository;
        this.reviewRepository = reviewRepository;
    }

    public void addReviewVote(ReviewVotesRequest reviewVotesRequest){
        if(reviewRepository.findById(reviewVotesRequest.reviewId()).isEmpty()){
            throw new IllegalArgumentException("Review with id " + reviewVotesRequest.reviewId() + " not found");
        }
        reviewVotesRepository.save(fromDTO(reviewVotesRequest));
    }

    public List<ReviewVotesResponse> getReviewVotesByReviewId(UUID reviewId){
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if(review == null){
            throw new IllegalArgumentException("Review with id " + reviewId + " not found");
        }
        List<ReviewVotesResponse> reviewVotesResponses = reviewVotesRepository.findReviewVoteByReview(review);
        if(reviewVotesResponses.isEmpty()){
            throw new IllegalArgumentException("ReviewVotes with review id " + reviewId + " not found");
        }
        return reviewVotesResponses;
    }

    public ReviewVote fromDTO(ReviewVotesRequest reviewVotesRequest) {
        Review review = reviewRepository.findById(reviewVotesRequest.reviewId()).orElse(null);
        if(review == null){
            throw new IllegalArgumentException("Review with id " + reviewVotesRequest.reviewId() + " not found");
        }
        ReviewVote reviewVote = new ReviewVote();
        reviewVote.setReview(review);
        reviewVote.setUserId(reviewVotesRequest.userId());
        return reviewVote;
    }
}

