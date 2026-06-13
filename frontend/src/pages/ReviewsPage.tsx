import { useState } from "react";
import { useAuth } from "@/context/AuthContext";
import * as reviewApi from "@/api/review";
import type { ApiResult } from "@/types";
import { SEED_CONTENT_IDS } from "@/types";
import { ActionButton, Field, PageHeader, ResponsePanel } from "@/components/ui";

export function ReviewsPage() {
  const { user } = useAuth();
  const [movieId, setMovieId] = useState<string>(SEED_CONTENT_IDS[0]);
  const [rating, setRating] = useState(5);
  const [reviewText, setReviewText] = useState("A solid pick for testing the review service.");
  const [spoiler, setSpoiler] = useState(false);
  const [lookupId, setLookupId] = useState("1");
  const [lastAction, setLastAction] = useState<ApiResult<unknown> | null>(null);
  const [lookup, setLookup] = useState<ApiResult<unknown> | null>(null);
  const [loading, setLoading] = useState(false);

  async function submitRating() {
    if (!user) return;
    setLoading(true);
    const result = await reviewApi.addRating(user.id, movieId, rating);
    setLastAction(result);
    setLoading(false);
  }

  async function submitReview() {
    if (!user) return;
    setLoading(true);
    const result = await reviewApi.addReview(user.id, movieId, reviewText, spoiler);
    setLastAction(result);
    setLoading(false);
  }

  async function fetchReview() {
    setLoading(true);
    const result = await reviewApi.getReview(lookupId);
    setLookup(result);
    setLoading(false);
  }

  return (
    <div className="page">
      <PageHeader
        title="Reviews & ratings"
        description="Submit ratings and reviews for catalog content. Uses movieId (content UUID). No JWT required on this service."
        service="review-rating-service"
        port={8085}
      />

      <div className="two-col">
        <section className="panel">
          <Field label="Content / movie ID">
            <select value={movieId} onChange={(e) => setMovieId(e.target.value)}>
              {SEED_CONTENT_IDS.map((id) => (
                <option key={id} value={id}>{id}</option>
              ))}
            </select>
          </Field>
          <Field label="Rating (1–5)">
            <input
              type="number"
              min={1}
              max={5}
              value={rating}
              onChange={(e) => setRating(Number(e.target.value))}
            />
          </Field>
          <ActionButton label="Submit rating" onClick={submitRating} disabled={loading} />

          <Field label="Review text">
            <textarea
              rows={4}
              value={reviewText}
              onChange={(e) => setReviewText(e.target.value)}
            />
          </Field>
          <label className="checkbox">
            <input type="checkbox" checked={spoiler} onChange={(e) => setSpoiler(e.target.checked)} />
            Contains spoilers
          </label>
          <ActionButton label="Submit review" onClick={submitReview} disabled={loading} />
          <ResponsePanel title="Submit result" result={lastAction} loading={loading} />
        </section>

        <section className="panel">
          <Field label="Lookup review by ID">
            <input value={lookupId} onChange={(e) => setLookupId(e.target.value)} />
          </Field>
          <ActionButton label="Fetch review" onClick={fetchReview} variant="secondary" disabled={loading} />
          <ResponsePanel title="Lookup" result={lookup} loading={loading} />
        </section>
      </div>
    </div>
  );
}
