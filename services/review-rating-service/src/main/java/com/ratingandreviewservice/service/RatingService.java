package com.ratingandreviewservice.service;

import com.ratingandreviewservice.dto.RatingRequest;
import com.ratingandreviewservice.dto.RatingResponse;
import com.ratingandreviewservice.model.Rating;
import com.ratingandreviewservice.repository.RatingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public void addRating(RatingRequest rating){
        Rating newRating = fromDTO(rating);
        newRating.setId(UUID.randomUUID());
        Rating savedRating = ratingRepository.save(newRating);
        if(savedRating.getId()==null){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save rating");
        }
    }

    public RatingResponse getRatingById(UUID id){
        Rating rating = ratingRepository.findById(id).orElse(null);
        if(rating == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating with id " + id + " not found");
        }

        return toDTO(rating);
    }

    public RatingResponse toDTO(Rating rating){
        return new RatingResponse(rating.getId(), rating.getUserId(), rating.getMovieId() , rating.getUserRating());
    }

    public Rating fromDTO(RatingRequest ratingDTO){
        Rating rating = new Rating();
        rating.setUserId(ratingDTO.userId());
        rating.setMovieId(ratingDTO.movieId());
        rating.setUserRating(ratingDTO.userRating());
        return rating;
    }

}
