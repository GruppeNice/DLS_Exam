package com.ratingandreviewservice.service;

import com.ratingandreviewservice.dto.RatingRequest;
import com.ratingandreviewservice.dto.RatingResponse;
import com.ratingandreviewservice.messaging.ReviewEventPublisher;
import com.ratingandreviewservice.model.Rating;
import com.ratingandreviewservice.repository.RatingRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ReviewEventPublisher eventPublisher;

    public RatingService(RatingRepository ratingRepository, ReviewEventPublisher eventPublisher) {
        this.ratingRepository = ratingRepository;
        this.eventPublisher = eventPublisher;
    }

    public void addRating(RatingRequest rating) {
        Rating existing = ratingRepository
            .findByUserIdAndMovieId(rating.userId(), rating.movieId())
            .orElse(null);

        Rating savedRating;
        if (existing != null) {
            existing.setUserRating(rating.userRating());
            savedRating = ratingRepository.save(existing);
        } else {
            Rating newRating = fromDTO(rating);
            newRating.setId(UUID.randomUUID());
            newRating.setCreatedAt(LocalDateTime.now());
            savedRating = ratingRepository.save(newRating);
        }

        if (savedRating.getId() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save rating");
        }
        try {
            eventPublisher.contentRated(
                savedRating.getId(),
                savedRating.getUserId(),
                savedRating.getMovieId(),
                savedRating.getUserRating(),
                Instant.now()
            );
        } catch (RuntimeException ignored) {
            // Rating is persisted; event delivery is best-effort for the demo UI.
        }
    }

    public RatingResponse getRatingById(UUID id) {
        Rating rating = ratingRepository.findById(id).orElse(null);
        if (rating == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating with id " + id + " not found");
        }

        return toDTO(rating);
    }

    public RatingResponse toDTO(Rating rating) {
        return new RatingResponse(rating.getId(), rating.getUserId(), rating.getMovieId(), rating.getUserRating());
    }

    public Rating fromDTO(RatingRequest ratingDTO) {
        Rating rating = new Rating();
        rating.setUserId(ratingDTO.userId());
        rating.setMovieId(ratingDTO.movieId());
        rating.setUserRating(ratingDTO.userRating());
        return rating;
    }
}
