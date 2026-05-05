2.2. Microservices Description


Responsibilities
Data ownership
API
Events Produced
User Management Service:
User registration and login
JWT token issuance
OAuth integration (e.g., Google)
Password reset workflow
Account status management (active, suspended, banned)
Basic profile data (email, display name)
Users 

Credentials

Roles

Account state
REST
Event
UserRegistered
UserSuspended
UserDeleted
Catalog Service  (GraphQL):
Movie and TV show metadata
Genres, tags, categories
Cast and crew information, release dates
Region-based availability
Content search and filtering
Content metadata

Availability rules

Search indexes
GraphQL
Produced
Consumed
ContentCreated
ContentRated
ContentUpdated
ContentReviewed
ContentRemoved
Streaming / Playback Service:
Start / stop / resume playback sessions
Track watch progress
Validate active subscription before streaming
DRM validation hook (simulated)
—
REST
Event
PlaybackStarted
PlaybackStopped
PlaybackProgressUpdated
Payment & Billing Service:
Subscription plan definitions and activation / cancellation
Payment processing and invoice generation
Refund handling
Payment gateway integration (simulated)
Subscription records

Payment transactions

Invoices


REST
Event
SubscriptionActivated
SubscriptionCancelled
PaymentSucceeded
PaymentFailed
Review & Rating Service:
Create / update / delete ratings (1–5 stars)
Written reviews and basic moderation status
Aggregate rating calculation
Ratings

Reviews

Moderation flags
REST
Event
ContentRated
ContentReviewed
ReviewModerated

Recommendation Service  (AI):


Consume playback and rating events
Build user preference models
Generate personalised "Recommended for You" lists
Calculate trending / popular content rankings
Periodic model retraining
Recommendation models

User preference vectors

Precomputed recommendation lists
REST
Event
PlaybackStarted
PlaybackProgressUpdated
ContentRated
SubscriptionActivated

Engagement Service:
Generate email, push, and in-app notifications
Personalised "Continue Watching" reminders
Re-engagement campaigns and delivery tracking
—
Async-
driven (primary) 

Minimal REST for manual triggers
Event
SubscriptionActivated
PlaybackStopped
ContentCreated



2.3. Communication between microservices
Asynchronous messaging via RabbitMQ is the primary communication method between all backend services. Direct service-to-service calls are avoided. Consumers include the Recommendation Service, Engagement Service, and a future Analytics Service.
Asynchronous Event Flow
Producer
Events
Streaming Service
PlaybackStarted, PlaybackStopped, PlaybackProgressUpdated
Review Service
ContentRated, ContentReviewed
Billing Service
SubscriptionActivated, SubscriptionCancelled, PaymentSucceeded, PaymentFailed
Catalog Service
ContentCreated, ContentUpdated, ContentRemoved



2.4. Description of the patterns and techniques used in the project.
Distributed Design Patterns
Pattern
Applied In
CQRS
Command-based Streaming · Query-based Catalog & Recommendations
Immutable Events
Playback events
Tombstone Pattern
Deleted reviews and content
Idempotency
Streaming and billing operations
Saga Pattern
Subscription activation workflow
Commutative Aggregation
Recommendations and trending calculations
Serverless / KEDA ScaledJob
Engagement Service background jobs
REST + GraphQL
Interoperability across services
AI Integration
Recommendation Service as real business workflow

