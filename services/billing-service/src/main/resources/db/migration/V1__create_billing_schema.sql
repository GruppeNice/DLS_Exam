CREATE TABLE subscription_plans (
    id CHAR(36) PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    price_cents BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    billing_period_days INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE subscriptions (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    plan_id CHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP(6) NOT NULL,
    ends_at TIMESTAMP(6) NOT NULL,
    cancelled_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_subscriptions_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans (id)
);

CREATE TABLE payment_transactions (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    subscription_id CHAR(36) NULL,
    plan_id CHAR(36) NULL,
    amount_cents BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    gateway_reference VARCHAR(120) NULL,
    failure_reason VARCHAR(500) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_payments_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions (id),
    CONSTRAINT fk_payments_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans (id)
);

CREATE TABLE invoices (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    payment_transaction_id CHAR(36) NOT NULL,
    subscription_id CHAR(36) NULL,
    amount_cents BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    issued_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_invoices_payment FOREIGN KEY (payment_transaction_id) REFERENCES payment_transactions (id),
    CONSTRAINT fk_invoices_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions (id)
);

CREATE UNIQUE INDEX idx_payments_idempotency_key ON payment_transactions (idempotency_key);
CREATE INDEX idx_subscriptions_user_id ON subscriptions (user_id);
CREATE INDEX idx_subscriptions_user_status ON subscriptions (user_id, status);
CREATE INDEX idx_invoices_user_id ON invoices (user_id);
