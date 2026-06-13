package com.dlsexam.catalogservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "content_tags")
@IdClass(ContentTagLinkId.class)
public class ContentTagLink {

    @Id
    @Column(name = "content_id")
    private UUID contentId;

    @Id
    @Column(name = "tag_id")
    private UUID tagId;

    public UUID getContentId() {
        return contentId;
    }

    public void setContentId(UUID contentId) {
        this.contentId = contentId;
    }

    public UUID getTagId() {
        return tagId;
    }

    public void setTagId(UUID tagId) {
        this.tagId = tagId;
    }
}
