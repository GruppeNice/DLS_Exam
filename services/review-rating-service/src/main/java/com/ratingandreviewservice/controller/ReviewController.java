package com.ratingandreviewservice.controller;

import com.ratingandreviewservice.dto.ReviewRequest;
import com.ratingandreviewservice.dto.ReviewResponse;
import com.ratingandreviewservice.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService){
        this.reviewService = reviewService;
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByMovieId(@PathVariable UUID movieId) {
        return ResponseEntity.ok(reviewService.getReviewsByMovieId(movieId));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable UUID reviewId){
        ReviewResponse reviewResponse = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(reviewResponse);
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addReview(@Valid @RequestBody ReviewRequest review){
        reviewService.addReview(review);
        return ResponseEntity.ok().build();
    }
}
