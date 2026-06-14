CREATE TABLE rating (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    movie_id CHAR(36) NOT NULL,
    user_rating INT NOT NULL,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    CONSTRAINT uk_rating_user_movie UNIQUE (user_id, movie_id)
);

CREATE TABLE review (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    movie_id CHAR(36) NOT NULL,
    review_text VARCHAR(2000),
    spoiler BIT(1),
    created_at DATE,
    updated_at DATE,
    CONSTRAINT uk_review_user_movie UNIQUE (user_id, movie_id)
);

CREATE TABLE review_vote (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    review_id CHAR(36) NOT NULL,
    vote_value INT NOT NULL,
    CONSTRAINT uk_review_vote_review_user UNIQUE (review_id, user_id),
    CONSTRAINT fk_review_vote_review FOREIGN KEY (review_id) REFERENCES review (id) ON DELETE CASCADE
);
