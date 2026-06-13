INSERT INTO genres (id, name, slug, created_at) VALUES
    ('11111111-1111-1111-1111-111111111101', 'Action', 'action', NOW()),
    ('11111111-1111-1111-1111-111111111102', 'Drama', 'drama', NOW()),
    ('11111111-1111-1111-1111-111111111103', 'Sci-Fi', 'sci-fi', NOW());

INSERT INTO tags (id, name, created_at) VALUES
    ('22222222-2222-2222-2222-222222222201', 'featured', NOW()),
    ('22222222-2222-2222-2222-222222222202', 'new-release', NOW());

INSERT INTO content_items (
    id, title, description, content_type, release_date, duration_minutes, poster_url,
    deleted, created_at, updated_at
) VALUES
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
        'Neon Horizon',
        'A cyberpunk thriller set in a rain-soaked megacity.',
        'MOVIE',
        '2024-03-15',
        118,
        'https://example.com/posters/neon-horizon.jpg',
        FALSE,
        NOW(),
        NOW()
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
        'Echoes of Mars',
        'A crew discovers ancient signals beneath the red planet.',
        'TV_SHOW',
        '2023-09-01',
        45,
        'https://example.com/posters/echoes-of-mars.jpg',
        FALSE,
        NOW(),
        NOW()
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3',
        'Midnight Express',
        'A heist crew races against time before dawn.',
        'MOVIE',
        '2022-11-20',
        102,
        'https://example.com/posters/midnight-express.jpg',
        FALSE,
        NOW(),
        NOW()
    );

INSERT INTO content_genres (content_id, genre_id) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '11111111-1111-1111-1111-111111111101'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '11111111-1111-1111-1111-111111111103'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', '11111111-1111-1111-1111-111111111103'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', '11111111-1111-1111-1111-111111111102'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', '11111111-1111-1111-1111-111111111101');

INSERT INTO content_tags (content_id, tag_id) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '22222222-2222-2222-2222-222222222201'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', '22222222-2222-2222-2222-222222222202');

INSERT INTO cast_members (id, content_id, person_name, role_type, created_at) VALUES
    ('33333333-3333-3333-3333-333333333301', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'Alex Rivera', 'ACTOR', NOW()),
    ('33333333-3333-3333-3333-333333333302', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'Jordan Lee', 'DIRECTOR', NOW()),
    ('33333333-3333-3333-3333-333333333303', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'Morgan Blake', 'ACTOR', NOW());

INSERT INTO content_availability (id, content_id, region_code, available_from, available_until, created_at) VALUES
    ('44444444-4444-4444-4444-444444444401', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'US', '2024-01-01', NULL, NOW()),
    ('44444444-4444-4444-4444-444444444402', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'EU', '2024-02-01', NULL, NOW()),
    ('44444444-4444-4444-4444-444444444403', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'US', '2023-09-01', NULL, NOW()),
    ('44444444-4444-4444-4444-444444444404', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'US', '2022-11-20', NULL, NOW());

INSERT INTO content_rating_stats (content_id, average_rating, rating_count, review_count, updated_at) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 4.20, 12, 5, NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 3.80, 8, 3, NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 4.50, 20, 9, NOW());
