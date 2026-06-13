package com.dlsexam.catalogservice.repository;

import com.dlsexam.catalogservice.domain.ContentGenreLink;
import com.dlsexam.catalogservice.domain.ContentGenreLinkId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentGenreLinkRepository extends JpaRepository<ContentGenreLink, ContentGenreLinkId> {

    List<ContentGenreLink> findByContentId(UUID contentId);

    @Modifying
    @Query("DELETE FROM ContentGenreLink l WHERE l.contentId = :contentId")
    void deleteByContentId(@Param("contentId") UUID contentId);
}
