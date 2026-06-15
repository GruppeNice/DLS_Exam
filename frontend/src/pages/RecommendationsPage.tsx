import { useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import { useCatalog } from "@/hooks/useCatalog";
import * as recommendationApi from "@/api/recommendation";
import {
  contentLookup,
  formatRecommendationScore,
  recommendationModelLabel,
  recommendationReasonLabel,
} from "@/lib/recommendations";
import type { ApiResult, RecommendationItem } from "@/types";
import { ActionButton, Field, ResponsePanel, StreamPageHeader } from "@/components/ui";

interface PersonalizedData {
  user_id: string;
  items: RecommendationItem[];
  generated_at: string;
  model_type: string;
}

interface TrendingData {
  items: RecommendationItem[];
  generated_at: string;
}

function RecommendationRows({
  items,
  catalog,
  emptyMessage,
}: {
  items: RecommendationItem[];
  catalog: ReturnType<typeof useCatalog>["items"];
  emptyMessage: string;
}) {
  if (items.length === 0) {
    return <p className="muted">{emptyMessage}</p>;
  }

  return (
    <ol className="recommendation-list">
      {items.map((item, index) => {
        const content = contentLookup(catalog, item.content_id);
        return (
          <li key={item.content_id} className="recommendation-row panel">
            <span className="recommendation-rank">{index + 1}</span>
            <div className="recommendation-poster">
              {content?.posterUrl ? (
                <img src={content.posterUrl} alt="" />
              ) : (
                <div className="poster-fallback">{content?.title?.[0] ?? "?"}</div>
              )}
            </div>
            <div className="recommendation-body">
              <h3>{content?.title ?? "Unknown title"}</h3>
              {content?.contentType && (
                <p className="content-type">{content.contentType}</p>
              )}
              <p className="recommendation-why">{recommendationReasonLabel(item.reason)}</p>
              <p className="muted recommendation-score">{formatRecommendationScore(item)}</p>
              {content?.genres && content.genres.length > 0 && (
                <div className="genre-tags">
                  {content.genres.map((genre) => (
                    <span key={genre.id} className="genre-tag">
                      {genre.name}
                    </span>
                  ))}
                </div>
              )}
              <Link to={`/playback?content=${item.content_id}`} className="watch-link">
                Watch now →
              </Link>
            </div>
          </li>
        );
      })}
    </ol>
  );
}

export function RecommendationsPage() {
  const { token, user } = useAuth();
  const { items: catalog, loading: catalogLoading } = useCatalog();
  const [contentId, setContentId] = useState<string>("");
  const [personal, setPersonal] = useState<ApiResult<PersonalizedData> | null>(null);
  const [trending, setTrending] = useState<ApiResult<TrendingData> | null>(null);
  const [retrain, setRetrain] = useState<ApiResult<unknown> | null>(null);
  const [ingest, setIngest] = useState<ApiResult<unknown> | null>(null);
  const [loading, setLoading] = useState(false);
  const [showDevTools, setShowDevTools] = useState(false);

  const selectedContentId = contentId || catalog[0]?.id || "";

  async function loadPersonal() {
    if (!token) return;
    setLoading(true);
    setPersonal(await recommendationApi.getMyRecommendations(token));
    setLoading(false);
  }

  async function loadTrending() {
    setLoading(true);
    setTrending(await recommendationApi.getTrending());
    setLoading(false);
  }

  async function runRetrain() {
    setLoading(true);
    const result = await recommendationApi.retrain();
    setRetrain(result);
    if (result.ok && token) {
      setPersonal(await recommendationApi.getMyRecommendations(token));
    }
    setLoading(false);
  }

  async function runIngest() {
    if (!user || !selectedContentId) return;
    setLoading(true);
    setIngest(
      await recommendationApi.ingestInteraction(user.id, selectedContentId),
    );
    setLoading(false);
  }

  const retrainRun =
    retrain?.ok && retrain.data && typeof retrain.data === "object"
      ? (retrain.data as {
          status?: string;
          user_count?: number;
          content_count?: number;
          message?: string;
        })
      : null;

  return (
    <div className="page">
      <StreamPageHeader
        title="For You & Trending"
        description="Trending updates live from watch and rating activity. For me only changes after you retrain the ML model."
      />

      <section className="panel">
        <div className="btn-row">
          <ActionButton label="Refresh for me" onClick={loadPersonal} disabled={loading || !token} />
          <ActionButton
            label="Refresh trending"
            onClick={loadTrending}
            variant="secondary"
            disabled={loading}
          />
        </div>
        {user && (
          <p className="muted recommendation-hint">
            Personalizing for <strong>{user.displayName || user.email}</strong> — watch and
            rate while logged in as this account.
          </p>
        )}
        <p className="muted recommendation-hint">
          <strong>Trending</strong> refreshes immediately after you watch or rate.
          <strong> For me</strong> stays empty until you open Developer tools and click{" "}
          <strong>Retrain model</strong>.
        </p>
      </section>

      <div className="two-col recommendations-cols">
        <section className="panel">
          <div className="recommendation-section-header">
            <h2>For me</h2>
            {personal?.ok && personal.data?.model_type && (
              <span className="badge badge-muted">
                {recommendationModelLabel(personal.data.model_type)}
              </span>
            )}
            {loading && personal === null && (
              <span className="badge badge-muted">Loading…</span>
            )}
          </div>
          {personal && !personal.ok && (
            <p className="form-error">{personal.error ?? "Could not load recommendations"}</p>
          )}
          {personal?.ok && personal.data && (
            <>
              <p className="muted recommendation-meta">
                Updated {new Date(personal.data.generated_at).toLocaleString()}
              </p>
              <RecommendationRows
                items={personal.data.items}
                catalog={catalog}
                emptyMessage={
                  personal.data.model_type === "awaiting_retrain"
                    ? "No ML picks yet. Watch or rate a few titles, then Retrain model in Developer tools below."
                    : "No personalized picks yet."
                }
              />
            </>
          )}
          {!personal && !loading && (
            <p className="muted">Click &quot;Refresh for me&quot; to load your list.</p>
          )}
        </section>

        <section className="panel">
          <div className="recommendation-section-header">
            <h2>Trending now</h2>
            {loading && trending === null && (
              <span className="badge badge-muted">Loading…</span>
            )}
          </div>
          {trending && !trending.ok && (
            <p className="form-error">{trending.error ?? "Could not load trending"}</p>
          )}
          {trending?.ok && trending.data && (
            <>
              <p className="muted recommendation-meta">
                Updated {new Date(trending.data.generated_at).toLocaleString()}
              </p>
              <RecommendationRows
                items={trending.data.items}
                catalog={catalog}
                emptyMessage="No trending data yet. Watch or rate content to build activity scores."
              />
            </>
          )}
          {!trending && !loading && (
            <p className="muted">Click &quot;Refresh trending&quot; to load the chart.</p>
          )}
        </section>
      </div>

      <section className="panel dev-tools-panel">
        <button
          type="button"
          className="dev-tools-toggle"
          onClick={() => setShowDevTools((open) => !open)}
          aria-expanded={showDevTools}
        >
          {showDevTools ? "▾" : "▸"} Developer tools (ingest & retrain)
        </button>

        {showDevTools && (
          <div className="dev-tools-body">
            <p className="muted">
              <strong>Ingest</strong> manually records a watch signal for testing.
              <strong> Retrain</strong> rebuilds the NMF model from all stored interactions.
            </p>
            <Field label="Simulate watch for">
              <select
                value={selectedContentId}
                onChange={(e) => setContentId(e.target.value)}
                disabled={catalogLoading || catalog.length === 0}
              >
                {catalog.map((item) => (
                  <option key={item.id} value={item.id}>
                    {item.title}
                  </option>
                ))}
              </select>
            </Field>
            <div className="btn-row">
              <ActionButton label="Ingest interaction" onClick={runIngest} disabled={loading} />
              <ActionButton
                label="Retrain model"
                onClick={runRetrain}
                variant="secondary"
                disabled={loading}
              />
            </div>
            {retrainRun && (
              <p className="muted retrain-summary">
                Retrain <strong>{retrainRun.status ?? "done"}</strong>
                {retrainRun.status === "SUCCEEDED" && retrainRun.user_count != null && (
                  <>
                    {" "}
                    — {retrainRun.user_count} user(s), {retrainRun.content_count ?? "?"} title(s)
                  </>
                )}
                {retrainRun.message && <> — {retrainRun.message}</>}
                {retrainRun.status === "SUCCEEDED" && <> &quot;For me&quot; was refreshed.</>}
              </p>
            )}
            <ResponsePanel title="Ingest / retrain (raw)" result={ingest ?? retrain} loading={loading} />
          </div>
        )}
      </section>
    </div>
  );
}
