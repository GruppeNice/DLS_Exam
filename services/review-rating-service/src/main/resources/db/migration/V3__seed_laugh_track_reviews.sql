-- Extra seeded reviews so Browse/Review UI has examples beyond the first three catalog titles.

INSERT INTO review (id, user_id, movie_id, review_text, spoiler, created_at, updated_at) VALUES
    ('b1111111-1111-4111-8111-111111111103', 'dddddddd-dddd-dddd-dddd-ddddddddddd2', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10', 'The mockumentary beats land every time.', FALSE, CURDATE(), NULL),
    ('b1111111-1111-4111-8111-111111111104', 'dddddddd-dddd-dddd-dddd-ddddddddddd1', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10', 'Best comfort-watch comedy in the catalog.', FALSE, CURDATE(), NULL);

INSERT INTO rating (id, user_id, movie_id, user_rating, created_at, updated_at) VALUES
    ('a1111111-1111-4111-8111-111111111104', 'dddddddd-dddd-dddd-dddd-ddddddddddd2', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10', 4, NOW(), NULL),
    ('a1111111-1111-4111-8111-111111111105', 'dddddddd-dddd-dddd-dddd-ddddddddddd1', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10', 5, NOW(), NULL);
