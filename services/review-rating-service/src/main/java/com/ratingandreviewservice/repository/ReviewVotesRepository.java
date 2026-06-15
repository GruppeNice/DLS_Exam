package com.ratingandreviewservice.repository;

import com.ratingandreviewservice.dto.ReviewVotesResponse;
import com.ratingandreviewservice.model.Review;
import com.ratingandreviewservice.model.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewVotesRepository extends JpaRepository<ReviewVote, UUID> {
    List<ReviewVotesResponse> findReviewVoteByReview(Review review);

    Optional<ReviewVote> findByReviewAndUserId(Review review, UUID userId);
}
