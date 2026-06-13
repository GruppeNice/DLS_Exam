package com.ratingandreviewservice.controller;

import com.ratingandreviewservice.dto.ReviewRequest;
import com.ratingandreviewservice.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService){
        this.reviewService = reviewService;
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<Void> getReviewById(@PathVariable("reviewId") UUID reviewId){
        reviewService.getReviewById(reviewId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addReview(@RequestBody ReviewRequest review){
        reviewService.addReview(review);
        return ResponseEntity.ok().build();
    }
}
