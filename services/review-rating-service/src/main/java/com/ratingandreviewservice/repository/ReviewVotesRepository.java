package com.ratingandreviewservice.repository;

import com.ratingandreviewservice.model.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewVotesRepository extends JpaRepository<ReviewVote,Integer> {


}
