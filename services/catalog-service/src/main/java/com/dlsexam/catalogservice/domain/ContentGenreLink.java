package com.dlsexam.catalogservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "content_genres")
@IdClass(ContentGenreLinkId.class)
public class ContentGenreLink {

    @Id
    @Column(name = "content_id")
    private UUID contentId;

    @Id
    @Column(name = "genre_id")
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
