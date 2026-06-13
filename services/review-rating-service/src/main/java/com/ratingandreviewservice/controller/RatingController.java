package com.ratingandreviewservice.controller;

import com.ratingandreviewservice.dto.RatingRequest;
import com.ratingandreviewservice.dto.RatingResponse;
import com.ratingandreviewservice.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping("/{ratingId}")
    public ResponseEntity<RatingResponse> getRatingById(@PathVariable UUID ratingId) {
        RatingResponse rating = ratingService.getRatingById(ratingId);
        return ResponseEntity.ok(rating);
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addRating(@RequestBody RatingRequest ratingRequest){
        ratingService.addRating(ratingRequest);
        return ResponseEntity.ok().build();
    }
}
