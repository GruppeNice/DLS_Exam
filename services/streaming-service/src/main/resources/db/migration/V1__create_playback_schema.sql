CREATE TABLE playback_sessions (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    content_id CHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    position_seconds BIGINT NOT NULL DEFAULT 0,
    drm_token VARCHAR(120) NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    started_at TIMESTAMP(6) NOT NULL,
    stopped_at TIMESTAMP(6) NULL,
    resumed_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

CREATE UNIQUE INDEX idx_playback_sessions_idempotency_key ON playback_sessions (idempotency_key);
CREATE INDEX idx_playback_sessions_user_id ON playback_sessions (user_id);
CREATE INDEX idx_playback_sessions_user_content ON playback_sessions (user_id, content_id);
CREATE INDEX idx_playback_sessions_user_status ON playback_sessions (user_id, status);
