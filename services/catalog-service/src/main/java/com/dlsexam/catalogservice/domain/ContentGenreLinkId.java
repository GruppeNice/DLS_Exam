package com.dlsexam.catalogservice.domain;

import java.io.Serializable;
import java.util.UUID;

public class ContentGenreLinkId implements Serializable {
    private UUID contentId;
    private UUID genreId;

    public UUID getContentId() {
        return contentId;
    }

    public void setContentId(UUID contentId) {
        this.contentId = contentId;
    }

    public UUID getGenreId() {
        return genreId;
    }

    public void setGenreId(UUID genreId) {
        this.genreId = genreId;
    }
}
