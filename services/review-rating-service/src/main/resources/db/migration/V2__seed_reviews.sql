-- Demo user and catalog content IDs are shared across services for local testing.
-- Demo user: dddddddd-dddd-dddd-dddd-ddddddddddd1 (see user-service V2__seed_demo_user.sql)
-- Catalog content: aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1/2/3 (see catalog-service V2__seed_catalog.sql)

INSERT INTO rating (id, user_id, movie_id, user_rating, created_at, updated_at) VALUES
    ('a1111111-1111-4111-8111-111111111101', 'dddddddd-dddd-dddd-dddd-ddddddddddd1', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 5, NOW(), NULL),
    ('a1111111-1111-4111-8111-111111111102', 'dddddddd-dddd-dddd-dddd-ddddddddddd1', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 4, NOW(), NULL),
    ('a1111111-1111-4111-8111-111111111103', 'dddddddd-dddd-dddd-dddd-ddddddddddd2', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 3, NOW(), NULL);

INSERT INTO review (id, user_id, movie_id, review_text, spoiler, created_at, updated_at) VALUES
    ('b1111111-1111-4111-8111-111111111101', 'dddddddd-dddd-dddd-dddd-ddddddddddd1', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'Neon Horizon is a standout cyberpunk thriller.', FALSE, CURDATE(), NULL),
    ('b1111111-1111-4111-8111-111111111102', 'dddddddd-dddd-dddd-dddd-ddddddddddd1', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'Slow burn but worth the finale.', TRUE, CURDATE(), NULL);

INSERT INTO review_vote (id, user_id, review_id, value) VALUES
    ('c1111111-1111-4111-8111-111111111101', 'dddddddd-dddd-dddd-dddd-ddddddddddd2', 'b1111111-1111-4111-8111-111111111101', 1);
