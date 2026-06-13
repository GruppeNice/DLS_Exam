INSERT INTO users (id, email, password_hash, display_name, status, created_at, updated_at)
VALUES (
    'dddddddd-dddd-dddd-dddd-ddddddddddd1',
    'demo@dls.local',
    '$2a$10$s3jXHv1eO8Tc5HYaomwFCOWoN8r.PeCxhcXvKCB1fd/B55lKC0kWe',
    'Demo User',
    'ACTIVE',
    NOW(),
    NOW()
);

INSERT INTO user_roles (user_id, role)
VALUES ('dddddddd-dddd-dddd-dddd-ddddddddddd1', 'USER');
