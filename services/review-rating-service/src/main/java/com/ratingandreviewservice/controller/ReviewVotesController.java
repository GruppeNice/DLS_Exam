package com.ratingandreviewservice.controller;

import com.ratingandreviewservice.dto.ReviewVotesRequest;
import com.ratingandreviewservice.dto.ReviewVotesResponse;
import com.ratingandreviewservice.service.ReviewVotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/review-votes")
public class ReviewVotesController {

    private final ReviewVotesService reviewVotesService;

   public ReviewVotesController(ReviewVotesService reviewVotesService){
        this.reviewVotesService = reviewVotesService;
   }

    @GetMapping("/{reviewId}")
    public ResponseEntity<List<ReviewVotesResponse>> getReviewVotesByReviewId(@PathVariable UUID reviewId) {
        List<ReviewVotesResponse> votes = reviewVotesService.getReviewVotesByReviewId(reviewId);
        return ResponseEntity.ok(votes);
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addReviewVote(@RequestBody ReviewVotesRequest request) {
        reviewVotesService.addReviewVote(request);
        return ResponseEntity.ok().build();
    }
}
