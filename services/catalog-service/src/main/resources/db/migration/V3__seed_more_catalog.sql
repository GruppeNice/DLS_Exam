INSERT INTO genres (id, name, slug, created_at) VALUES
    ('11111111-1111-1111-1111-111111111104', 'Comedy', 'comedy', NOW()),
    ('11111111-1111-1111-1111-111111111105', 'Horror', 'horror', NOW()),
    ('11111111-1111-1111-1111-111111111106', 'Documentary', 'documentary', NOW());

INSERT INTO tags (id, name, created_at) VALUES
    ('22222222-2222-2222-2222-222222222203', 'award-winner', NOW()),
    ('22222222-2222-2222-2222-222222222204', 'trending', NOW());

INSERT INTO content_items (
    id, title, description, content_type, release_date, duration_minutes, poster_url,
    deleted, created_at, updated_at
) VALUES
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4',
        'Silent Frequency',
        'A radio host picks up distress calls from a frequency that should not exist.',
        'MOVIE',
        '2024-06-01',
        96,
        'https://example.com/posters/silent-frequency.jpg',
        FALSE,
        NOW(),
        NOW()
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5',
        'Garden State of Mind',
        'An estranged siblings comedy about fixing a rooftop greenhouse before winter.',
        'MOVIE',
        '2023-04-12',
        110,
        'https://example.com/posters/garden-state-of-mind.jpg',
        FALSE,
        NOW(),
        NOW()
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6',
        'The Last Lighthouse',
        'A keeper documents storms and memories during the final season before automation.',
        'MOVIE',
        '2021-08-30',
        124,
        'https://example.com/posters/last-lighthouse.jpg',
        FALSE,
        NOW(),
        NOW()
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7',
        'Velocity Run',
        'Street racers and detectives collide in a midnight chase across the harbor district.',
        'MOVIE',
        '2024-01-19',
        99,
        'https://example.com/posters/velocity-run.jpg',
        FALSE,
        NOW(),
        NOW()
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa8',
        'Paper Planets',
        'A documentary following engineers who fold origami satellites for low-orbit science.',
        'MOVIE',
        '2022-05-05',
        88,
        'https://example.com/posters/paper-planets.jpg',
        FALSE,
        NOW(),
        NOW()
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9',
        'Twin Suns',
        'Colonists on a binary-star world uncover ruins beneath the desert dunes.',
        'TV_SHOW',
        '2024-09-10',
        52,
        'https://example.com/posters/twin-suns.jpg',
        FALSE,
        NOW(),
        NOW()
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10',
        'Laugh Track',
        'A sitcom writer room fights deadlines, notes, and their own punchlines.',
        'TV_SHOW',
        '2023-11-03',
        28,
        'https://example.com/posters/laugh-track.jpg',
        FALSE,
        NOW(),
        NOW()
    );

INSERT INTO content_genres (content_id, genre_id) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', '11111111-1111-1111-1111-111111111105'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', '11111111-1111-1111-1111-111111111103'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', '11111111-1111-1111-1111-111111111104'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', '11111111-1111-1111-1111-111111111102'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6', '11111111-1111-1111-1111-111111111102'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7', '11111111-1111-1111-1111-111111111101'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7', '11111111-1111-1111-1111-111111111103'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa8', '11111111-1111-1111-1111-111111111106'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa8', '11111111-1111-1111-1111-111111111103'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9', '11111111-1111-1111-1111-111111111103'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9', '11111111-1111-1111-1111-111111111102'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10', '11111111-1111-1111-1111-111111111104');

INSERT INTO content_tags (content_id, tag_id) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', '22222222-2222-2222-2222-222222222202'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6', '22222222-2222-2222-2222-222222222203'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7', '22222222-2222-2222-2222-222222222204'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9', '22222222-2222-2222-2222-222222222201'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10', '22222222-2222-2222-2222-222222222202');

INSERT INTO cast_members (id, content_id, person_name, role_type, created_at) VALUES
    ('33333333-3333-3333-3333-333333333304', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'Riley Chen', 'ACTOR', NOW()),
    ('33333333-3333-3333-3333-333333333305', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 'Sam Ortiz', 'ACTOR', NOW()),
    ('33333333-3333-3333-3333-333333333306', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7', 'Casey Wu', 'ACTOR', NOW()),
    ('33333333-3333-3333-3333-333333333307', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9', 'Taylor Brooks', 'ACTOR', NOW());

INSERT INTO content_availability (id, content_id, region_code, available_from, available_until, created_at) VALUES
    ('44444444-4444-4444-4444-444444444405', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'US', '2024-06-01', NULL, NOW()),
    ('44444444-4444-4444-4444-444444444406', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 'US', '2023-04-12', NULL, NOW()),
    ('44444444-4444-4444-4444-444444444407', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6', 'US', '2021-08-30', NULL, NOW()),
    ('44444444-4444-4444-4444-444444444408', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7', 'US', '2024-01-19', NULL, NOW()),
    ('44444444-4444-4444-4444-444444444409', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa8', 'US', '2022-05-05', NULL, NOW()),
    ('44444444-4444-4444-4444-444444444410', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9', 'US', '2024-09-10', NULL, NOW()),
    ('44444444-4444-4444-4444-444444444411', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10', 'US', '2023-11-03', NULL, NOW());

INSERT INTO content_rating_stats (content_id, average_rating, rating_count, review_count, updated_at) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 3.90, 6, 2, NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 4.10, 14, 6, NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6', 4.60, 22, 11, NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7', 3.70, 9, 4, NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa8', 4.30, 11, 5, NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9', 4.00, 7, 3, NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10', 3.50, 5, 2, NOW());
