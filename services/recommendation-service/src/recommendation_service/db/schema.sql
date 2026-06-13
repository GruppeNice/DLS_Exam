CREATE TABLE IF NOT EXISTS user_content_interactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    content_id CHAR(36) NOT NULL,
    interaction_weight DOUBLE NOT NULL DEFAULT 0,
    source_event VARCHAR(60) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_interactions_user_content (user_id, content_id)
);

CREATE TABLE IF NOT EXISTS user_recommendations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36) NOT NULL UNIQUE,
    content_ids JSON NOT NULL,
    generated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS trending_content (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content_id CHAR(36) NOT NULL UNIQUE,
    score DOUBLE NOT NULL DEFAULT 0,
    play_count INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS model_runs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    model_type VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_count INT NOT NULL DEFAULT 0,
    content_count INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    completed_at TIMESTAMP(6) NULL,
    details TEXT NULL
);

CREATE INDEX idx_interactions_user_id ON user_content_interactions (user_id);
CREATE INDEX idx_interactions_content_id ON user_content_interactions (content_id);
CREATE INDEX idx_trending_score ON trending_content (score DESC);
