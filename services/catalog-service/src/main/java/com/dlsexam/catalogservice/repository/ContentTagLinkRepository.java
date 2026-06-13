package com.dlsexam.catalogservice.repository;

import com.dlsexam.catalogservice.domain.ContentTagLink;
import com.dlsexam.catalogservice.domain.ContentTagLinkId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentTagLinkRepository extends JpaRepository<ContentTagLink, ContentTagLinkId> {

    List<ContentTagLink> findByContentId(UUID contentId);

    @Modifying
    @Query("DELETE FROM ContentTagLink l WHERE l.contentId = :contentId")
    void deleteByContentId(@Param("contentId") UUID contentId);
}
