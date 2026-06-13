CREATE TABLE IF NOT EXISTS user_content_interactions (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    content_id UUID NOT NULL,
    interaction_weight DOUBLE PRECISION NOT NULL DEFAULT 0,
    source_event VARCHAR(60) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, content_id)
);

CREATE TABLE IF NOT EXISTS user_recommendations (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    content_ids JSONB NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS trending_content (
    id SERIAL PRIMARY KEY,
    content_id UUID NOT NULL UNIQUE,
    score DOUBLE PRECISION NOT NULL DEFAULT 0,
    play_count INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS model_runs (
    id SERIAL PRIMARY KEY,
    model_type VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_count INTEGER NOT NULL DEFAULT 0,
    content_count INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_interactions_user_id ON user_content_interactions (user_id);
CREATE INDEX IF NOT EXISTS idx_interactions_content_id ON user_content_interactions (content_id);
CREATE INDEX IF NOT EXISTS idx_trending_score ON trending_content (score DESC);
