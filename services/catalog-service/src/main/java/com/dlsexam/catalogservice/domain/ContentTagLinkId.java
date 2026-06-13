package com.dlsexam.catalogservice.domain;

import java.io.Serializable;
import java.util.UUID;

public class ContentTagLinkId implements Serializable {
    private UUID contentId;
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
