package com.dlsexam.catalogservice.repository;

import com.dlsexam.catalogservice.domain.ContentAvailability;
import com.dlsexam.catalogservice.domain.ContentGenreLink;
import com.dlsexam.catalogservice.domain.ContentItem;
import com.dlsexam.catalogservice.domain.ContentTagLink;
import com.dlsexam.catalogservice.domain.ContentType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentItemRepository extends JpaRepository<ContentItem, UUID> {

    Optional<ContentItem> findByIdAndDeletedFalse(UUID id);

    @Query("""
        SELECT DISTINCT c FROM ContentItem c
        WHERE c.deleted = false
          AND (:titleContains IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :titleContains, '%')))
          AND (:contentType IS NULL OR c.contentType = :contentType)
          AND (:genreId IS NULL OR EXISTS (
              SELECT 1 FROM ContentGenreLink g WHERE g.contentId = c.id AND g.genreId = :genreId
          ))
          AND (:tagId IS NULL OR EXISTS (
              SELECT 1 FROM ContentTagLink t WHERE t.contentId = c.id AND t.tagId = :tagId
          ))
          AND (:regionCode IS NULL OR EXISTS (
              SELECT 1 FROM ContentAvailability a
              WHERE a.contentId = c.id AND a.regionCode = :regionCode
                AND (a.availableFrom IS NULL OR a.availableFrom <= CURRENT_DATE)
                AND (a.availableUntil IS NULL OR a.availableUntil >= CURRENT_DATE)
          ))
        """)
    Page<ContentItem> search(
        @Param("titleContains") String titleContains,
        @Param("contentType") ContentType contentType,
        @Param("genreId") UUID genreId,
        @Param("tagId") UUID tagId,
        @Param("regionCode") String regionCode,
        Pageable pageable
    );

    List<ContentItem> findByDeletedFalse();
}
