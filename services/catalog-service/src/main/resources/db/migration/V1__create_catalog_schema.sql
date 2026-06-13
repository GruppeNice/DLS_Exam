CREATE TABLE genres (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    slug VARCHAR(80) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE tags (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE content_items (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    content_type VARCHAR(20) NOT NULL,
    release_date DATE,
    duration_minutes INT,
    poster_url VARCHAR(500),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE content_genres (
    content_id UUID NOT NULL,
    genre_id UUID NOT NULL,
    PRIMARY KEY (content_id, genre_id),
    CONSTRAINT fk_content_genres_content FOREIGN KEY (content_id) REFERENCES content_items (id),
    CONSTRAINT fk_content_genres_genre FOREIGN KEY (genre_id) REFERENCES genres (id)
);

CREATE TABLE content_tags (
    content_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    PRIMARY KEY (content_id, tag_id),
    CONSTRAINT fk_content_tags_content FOREIGN KEY (content_id) REFERENCES content_items (id),
    CONSTRAINT fk_content_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
);

CREATE TABLE cast_members (
    id UUID PRIMARY KEY,
    content_id UUID NOT NULL,
    person_name VARCHAR(120) NOT NULL,
    role_type VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_cast_members_content FOREIGN KEY (content_id) REFERENCES content_items (id)
);

CREATE TABLE content_availability (
    id UUID PRIMARY KEY,
    content_id UUID NOT NULL,
    region_code VARCHAR(10) NOT NULL,
    available_from DATE,
    available_until DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_content_availability_content FOREIGN KEY (content_id) REFERENCES content_items (id)
);

CREATE TABLE content_rating_stats (
    content_id UUID PRIMARY KEY,
    average_rating NUMERIC(3, 2) NOT NULL DEFAULT 0,
    rating_count INT NOT NULL DEFAULT 0,
    review_count INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_content_rating_stats_content FOREIGN KEY (content_id) REFERENCES content_items (id)
);

CREATE INDEX idx_content_items_title ON content_items (title);
CREATE INDEX idx_content_items_type ON content_items (content_type);
CREATE INDEX idx_content_items_deleted ON content_items (deleted);
CREATE INDEX idx_content_availability_region ON content_availability (region_code);
CREATE INDEX idx_cast_members_content ON cast_members (content_id);
