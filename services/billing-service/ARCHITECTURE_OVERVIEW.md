# Billing Service Architecture Overview

This document explains what was created in `services/billing-service` and what each file is responsible for.

## Purpose of this service

The Billing Service (Payment & Billing) is the subscription and payment microservice for the DLS project. It provides:

- subscription plan definitions
- subscription activation and cancellation
- simulated payment gateway integration
- invoice generation
- refund handling
- billing-domain events published to RabbitMQ
- database schema management with Flyway

It aligns with the architecture docs: REST API, event-driven communication, saga pattern for subscription activation, and idempotency for billing operations.

---

## Folder structure

- `src/main/java/com/dlsexam/billingservice`: Java application code
- `src/main/resources`: application configuration and database migrations
- `src/test/java/com/dlsexam/billingservice`: test code
- `api`: OpenAPI and AsyncAPI contracts
- `config`: runtime environment templates
- project root files (`pom.xml`, `Dockerfile`, `docker-compose.yml`, `README.md`)

---

## Domain model

- `SubscriptionPlan`: plan catalog (code, price, billing period)
- `Subscription`: user subscription record with lifecycle status
- `PaymentTransaction`: payment attempt with idempotency key and gateway reference
- `Invoice`: issued invoice linked to a payment (and optionally subscription)

Statuses:

- subscriptions: `ACTIVE`, `CANCELLED`, `EXPIRED`
- payments: `PENDING`, `SUCCEEDED`, `FAILED`, `REFUNDED`
- invoices: `ISSUED`, `VOID`

---

## Core workflows

### Subscription activation saga

1. Validate plan and ensure user has no active subscription
2. Resolve or create idempotent payment record
3. Charge via simulated payment gateway
4. On success: persist subscription, generate invoice, publish `PaymentSucceeded` and `SubscriptionActivated`
5. On failure: publish `PaymentFailed` and return error (no subscription created)

### Cancellation and refunds

- Cancellation sets subscription to `CANCELLED` and publishes `SubscriptionCancelled`
- Refund reverses a succeeded payment, cancels linked active subscription, and updates payment status to `REFUNDED`

---

## API controllers

- `PlanController`: public plan catalog endpoints
- `SubscriptionController`: activation, cancellation, and active-subscription lookup
- `PaymentController`: standalone payment processing and refunds
- `InvoiceController`: user invoice listing

---

## Security

JWT validation only (tokens from User Service). Claims are read directly from the token; no local user database lookup.

Public endpoints:

- health/docs
- plan listing
- active subscription check (for Streaming Service)

---

## Messaging

`BillingEventPublisher` publishes:

- `subscription.activated`
- `subscription.cancelled`
- `payment.succeeded`
- `payment.failed`

Exchange: `billing.events` (configurable)

---

## Local runtime

- App port: `8084`
- PostgreSQL: `5433` (to avoid conflict with user-service on `5432`)
- RabbitMQ: `5673` / management `15673`

Use `docker compose up --build` for the full local stack.

---

## Notes on current maturity

This service is a scaffold aligned with the architecture docs. Future enhancements:

- full OpenAPI/AsyncAPI schemas
- distributed saga compensation with outbox pattern
- integration tests (DB + RabbitMQ + endpoint-level)
- admin plan management endpoints
- gateway-level integration once API gateway is implemented
