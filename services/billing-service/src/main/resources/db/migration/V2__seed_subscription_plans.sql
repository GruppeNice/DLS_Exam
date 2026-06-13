INSERT INTO subscription_plans (id, code, name, description, price_cents, currency, billing_period_days, active, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111101', 'BASIC', 'Basic', 'Standard streaming access', 999, 'USD', 30, TRUE, NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111102', 'PREMIUM', 'Premium', 'HD streaming with offline downloads', 1499, 'USD', 30, TRUE, NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111103', 'FAMILY', 'Family', 'Up to 4 profiles and premium features', 1999, 'USD', 30, TRUE, NOW(), NOW());
