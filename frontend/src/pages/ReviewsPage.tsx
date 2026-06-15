import { useCallback, useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import * as engagementApi from "@/api/engagement";
import * as reviewApi from "@/api/review";
import type { Review } from "@/api/review";
import { useCatalog } from "@/hooks/useCatalog";
import { ActionButton, StatusMessage, StreamPageHeader } from "@/components/ui";

const DEMO_USER_ID = "dddddddd-dddd-dddd-dddd-ddddddddddd1";

function reviewerLabel(userId: string, currentUserId?: string): string {
  if (userId === currentUserId) return "You";
  if (userId === DEMO_USER_ID) return "Demo User";
  return `Viewer ${userId.slice(0, 8)}…`;
}

function StarRating({ value }: { value: number }) {
  return (
    <span className="star-display" aria-label={`${value} out of 5 stars`}>
      {[1, 2, 3, 4, 5].map((star) => (
        <span key={star} className={star <= value ? "star-filled" : "star-empty"}>★</span>
      ))}
    </span>
  );
}

function ReviewVoteBar({
  review,
  currentUserId,
  titleName,
}: {
  review: Review;
  currentUserId?: string;
  titleName?: string;
}) {
  const [votes, setVotes] = useState<reviewApi.ReviewVote[]>([]);
  const [loadingVotes, setLoadingVotes] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [voteMessage, setVoteMessage] = useState<string | null>(null);
  const [voteError, setVoteError] = useState<string | null>(null);

  const isOwnReview = review.userId === currentUserId;

  const loadVotes = useCallback(async () => {
    setLoadingVotes(true);
    const result = await reviewApi.getReviewVotes(review.id);
    setVotes(result.ok && result.data ? result.data : []);
    setLoadingVotes(false);
  }, [review.id]);

  useEffect(() => {
    void loadVotes();
  }, [loadVotes]);

  const upvotes = votes.filter((vote) => vote.value === 1).length;
  const downvotes = votes.filter((vote) => vote.value === -1).length;
  const myVote = votes.find((vote) => vote.userId === currentUserId)?.value;

  async function castVote(value: 1 | -1) {
    if (!currentUserId || isOwnReview) return;
    setSubmitting(true);
    setVoteMessage(null);
    setVoteError(null);
    const result = await reviewApi.addReviewVote(currentUserId, review.id, value);
    if (result.ok) {
      await loadVotes();
      if (value === 1) {
        setVoteMessage(
          `Upvote recorded. If this is someone else's review, they get an email via MailHog${titleName ? ` (${titleName})` : ""}.`,
        );
      } else {
        setVoteMessage("Downvote recorded.");
      }
    } else {
      setVoteError(result.error ?? "Could not record vote");
    }
    setSubmitting(false);
  }

  return (
    <div className="review-vote-bar">
      <div className="review-vote-actions">
        <button
          type="button"
          className={`vote-btn up ${myVote === 1 ? "active" : ""}`}
          onClick={() => void castVote(1)}
          disabled={!currentUserId || isOwnReview || submitting || loadingVotes}
          title={isOwnReview ? "You cannot vote on your own review" : "Upvote"}
        >
          ▲ {upvotes}
        </button>
        <button
          type="button"
          className={`vote-btn down ${myVote === -1 ? "active" : ""}`}
          onClick={() => void castVote(-1)}
          disabled={!currentUserId || isOwnReview || submitting || loadingVotes}
          title={isOwnReview ? "You cannot vote on your own review" : "Downvote"}
        >
          ▼ {downvotes}
        </button>
      </div>
      {isOwnReview && <span className="muted review-vote-hint">Your review</span>}
      {voteMessage && <p className="muted review-vote-msg">{voteMessage}</p>}
      {voteError && <p className="form-error review-vote-msg">{voteError}</p>}
    </div>
  );
}

export function ReviewsPage() {
  const { user } = useAuth();
  const { items } = useCatalog();
  const [movieId, setMovieId] = useState(items[0]?.id ?? "");
  const [rating, setRating] = useState(5);
  const [reviewText, setReviewText] = useState("");
  const [spoiler, setSpoiler] = useState(false);
  const [titleReviews, setTitleReviews] = useState<Review[]>([]);
  const [reviewsLoading, setReviewsLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const selected = items.find((item) => item.id === movieId);

  const loadTitleReviews = useCallback(async () => {
    if (!movieId) {
      setTitleReviews([]);
      return;
    }
    setReviewsLoading(true);
    const result = await reviewApi.getReviewsByMovie(movieId);
    if (result.ok && result.data) {
      setTitleReviews(result.data);
    } else {
      setTitleReviews([]);
    }
    setReviewsLoading(false);
  }, [movieId]);

  useEffect(() => {
    if (!movieId && items[0]) {
      setMovieId(items[0].id);
    }
  }, [items, movieId]);

  useEffect(() => {
    void loadTitleReviews();
  }, [loadTitleReviews]);

  useEffect(() => {
    const mine = titleReviews.find((review) => review.userId === user?.id);
    if (mine) {
      setReviewText(mine.reviewText);
      setSpoiler(mine.spoiler);
      if (mine.userRating) setRating(mine.userRating);
    } else {
      setReviewText("");
      setSpoiler(false);
      setRating(5);
    }
  }, [titleReviews, user?.id, movieId]);

  async function publishReview() {
    if (!user || !movieId) return;
    if (!reviewText.trim()) {
      setError("Write a review before publishing — your star rating is saved with it.");
      return;
    }
    setLoading(true);
    setMessage(null);
    setError(null);
    const trimmed = reviewText.trim();
    const result = await reviewApi.addReview(user.id, movieId, trimmed, rating, spoiler);
    if (result.ok) {
      const notify = await engagementApi.sendNotification({
        type: "EMAIL",
        recipient: user.email,
        subject: `Your review of ${selected?.title ?? "a title"}`,
        templateName: "review-published",
        templateVariables: {
          name: user.displayName,
          title: selected?.title ?? "your pick",
          reviewText: trimmed,
          spoiler: spoiler ? "true" : "false",
        },
      });

      await loadTitleReviews();

      if (notify.ok) {
        setMessage(
          `Review and ${rating}-star rating published for ${selected?.title ?? "this title"}. Check MailHog for the email.`,
        );
      } else {
        setMessage(
          `Review and rating saved for ${selected?.title ?? "this title"}.`,
        );
      }
    } else {
      setError(result.error ?? "Could not publish review");
    }
    setLoading(false);
  }

  return (
    <div className="page">
      <StreamPageHeader
        title="Rate & review"
        description="Pick a star rating, write your review, and publish once. Reviews for the selected title appear on the right."
      />

      <div className="two-col review-layout">
        <section className="panel">
          <label className="field">
            <span className="field-label">Title</span>
            <select
              value={movieId}
              onChange={(e) => setMovieId(e.target.value)}
              disabled={items.length === 0}
            >
              {items.map((item) => (
                <option key={item.id} value={item.id}>{item.title}</option>
              ))}
            </select>
          </label>

          {selected && (
            <div className="review-title-card">
              <div className="poster-fallback small">{selected.title[0]}</div>
              <div>
                <h3>{selected.title}</h3>
                <p className="muted">{selected.description}</p>
              </div>
            </div>
          )}

          <label className="field">
            <span className="field-label">Your rating (required with review)</span>
            <div className="star-row">
              {[1, 2, 3, 4, 5].map((value) => (
                <button
                  key={value}
                  type="button"
                  className={`star-btn ${rating >= value ? "on" : ""}`}
                  onClick={() => setRating(value)}
                >
                  ★
                </button>
              ))}
            </div>
          </label>

          <label className="field">
            <span className="field-label">Your review</span>
            <textarea
              rows={5}
              value={reviewText}
              onChange={(e) => setReviewText(e.target.value)}
              placeholder="What did you think after watching?"
            />
          </label>
          <label className="checkbox">
            <input type="checkbox" checked={spoiler} onChange={(e) => setSpoiler(e.target.checked)} />
            Contains spoilers
          </label>
          <ActionButton
            label="Publish review"
            onClick={publishReview}
            disabled={loading || !movieId || !reviewText.trim()}
          />
          {message && <StatusMessage variant="success">{message}</StatusMessage>}
          {error && <StatusMessage variant="error">{error}</StatusMessage>}
        </section>

        <section className="panel reviews-list-panel">
          <div className="section-head">
            <h2>Reviews for {selected?.title ?? "this title"}</h2>
            <span className="badge badge-muted">{titleReviews.length}</span>
          </div>

          {reviewsLoading && <p className="muted">Loading reviews…</p>}

          {!reviewsLoading && titleReviews.length === 0 && (
            <p className="muted">No reviews yet. Be the first to publish one.</p>
          )}

          <ul className="review-feed">
            {titleReviews.map((review) => (
              <li key={review.id} className="review-card">
                <div className="review-card-head">
                  <strong>{reviewerLabel(review.userId, user?.id)}</strong>
                  {review.userRating != null && <StarRating value={review.userRating} />}
                  {review.spoiler && <span className="badge badge-error">Spoiler</span>}
                  {review.userId === user?.id && <span className="badge badge-ok">Yours</span>}
                </div>
                <p className="review-card-body">{review.reviewText}</p>
                <ReviewVoteBar
                  review={review}
                  currentUserId={user?.id}
                  titleName={selected?.title}
                />
                {review.createdAt && (
                  <p className="muted review-card-date">{review.createdAt}</p>
                )}
              </li>
            ))}
          </ul>

          <p className="muted review-mailhog-hint">
            After you publish or upvote someone else&apos;s review, open{" "}
            <a href="http://localhost:8025" target="_blank" rel="noreferrer">MailHog</a>
            {" "}to see the notification email.
          </p>
        </section>
      </div>
    </div>
  );
}
