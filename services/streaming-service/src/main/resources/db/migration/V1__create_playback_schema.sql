CREATE TABLE playback_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    content_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    position_seconds BIGINT NOT NULL DEFAULT 0,
    drm_token VARCHAR(120),
    idempotency_key VARCHAR(120) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    stopped_at TIMESTAMP WITH TIME ZONE,
    resumed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX idx_playback_sessions_idempotency_key ON playback_sessions (idempotency_key);
CREATE INDEX idx_playback_sessions_user_id ON playback_sessions (user_id);
CREATE INDEX idx_playback_sessions_user_content ON playback_sessions (user_id, content_id);
CREATE INDEX idx_playback_sessions_user_status ON playback_sessions (user_id, status);
