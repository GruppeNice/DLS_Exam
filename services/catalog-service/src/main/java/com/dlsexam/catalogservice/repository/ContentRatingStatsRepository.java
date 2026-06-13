package com.dlsexam.catalogservice.repository;

import com.dlsexam.catalogservice.domain.ContentRatingStats;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRatingStatsRepository extends JpaRepository<ContentRatingStats, UUID> {
}
