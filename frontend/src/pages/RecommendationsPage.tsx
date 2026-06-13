import { useState } from "react";
import { useAuth } from "@/context/AuthContext";
import * as recommendationApi from "@/api/recommendation";
import type { ApiResult } from "@/types";
import { SEED_CONTENT_IDS } from "@/types";
import { ActionButton, Field, PageHeader, ResponsePanel } from "@/components/ui";

export function RecommendationsPage() {
  const { token, user } = useAuth();
  const [contentId, setContentId] = useState<string>(SEED_CONTENT_IDS[0]);
  const [personal, setPersonal] = useState<ApiResult<unknown> | null>(null);
  const [trending, setTrending] = useState<ApiResult<unknown> | null>(null);
  const [retrain, setRetrain] = useState<ApiResult<unknown> | null>(null);
  const [ingest, setIngest] = useState<ApiResult<unknown> | null>(null);
  const [loading, setLoading] = useState(false);

  async function loadPersonal() {
    if (!token) return;
    setLoading(true);
    const result = await recommendationApi.getMyRecommendations(token);
    setPersonal(result);
    setLoading(false);
  }

  async function loadTrending() {
    setLoading(true);
    const result = await recommendationApi.getTrending();
    setTrending(result);
    setLoading(false);
  }

  async function runRetrain() {
    setLoading(true);
    const result = await recommendationApi.retrain();
    setRetrain(result);
    setLoading(false);
  }

  async function runIngest() {
    if (!user) return;
    setLoading(true);
    const result = await recommendationApi.ingestInteraction(user.id, contentId);
    setIngest(result);
    setLoading(false);
  }

  return (
    <div className="page">
      <PageHeader
        title="Recommendations"
        description="Personalized lists (JWT), trending content, manual interaction ingest, and model retrain."
        service="recommendation-service"
        port={8090}
      />

      <section className="panel">
        <div className="btn-row">
          <ActionButton label="For me" onClick={loadPersonal} disabled={loading} />
          <ActionButton label="Trending" onClick={loadTrending} variant="secondary" disabled={loading} />
          <ActionButton label="Retrain model" onClick={runRetrain} variant="secondary" disabled={loading} />
        </div>
      </section>

      <div className="two-col">
        <section className="panel">
          <Field label="Content ID for interaction">
            <select value={contentId} onChange={(e) => setContentId(e.target.value)}>
              {SEED_CONTENT_IDS.map((id) => (
                <option key={id} value={id}>{id}</option>
              ))}
            </select>
          </Field>
          <ActionButton label="Ingest interaction" onClick={runIngest} disabled={loading} />
          <ResponsePanel title="Ingest / retrain" result={ingest ?? retrain} loading={loading} />
        </section>

        <ResponsePanel title="Personalized" result={personal} loading={loading} />
      </div>

      <ResponsePanel title="Trending" result={trending} loading={loading} />
    </div>
  );
}
