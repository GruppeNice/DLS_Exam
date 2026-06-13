import { useState } from "react";
import { useAuth } from "@/context/AuthContext";
import * as streamingApi from "@/api/streaming";
import type { ApiResult, PlaybackSession } from "@/types";
import { SEED_CONTENT_IDS } from "@/types";
import { ActionButton, Field, PageHeader, ResponsePanel } from "@/components/ui";

export function PlaybackPage() {
  const { token } = useAuth();
  const [contentId, setContentId] = useState<string>(SEED_CONTENT_IDS[0]);
  const [sessionId, setSessionId] = useState("");
  const [position, setPosition] = useState(300);
  const [sessions, setSessions] = useState<ApiResult<PlaybackSession[]> | null>(null);
  const [lastAction, setLastAction] = useState<ApiResult<PlaybackSession> | null>(null);
  const [loading, setLoading] = useState(false);

  async function loadSessions() {
    if (!token) return;
    setLoading(true);
    const result = await streamingApi.getMySessions(token);
    setSessions(result);
    if (result.ok && result.data?.[0]) {
      setSessionId(result.data[0].id);
    }
    setLoading(false);
  }

  async function start() {
    if (!token) return;
    setLoading(true);
    const result = await streamingApi.startPlayback(token, contentId);
    setLastAction(result);
    if (result.ok && result.data) setSessionId(result.data.id);
    setLoading(false);
  }

  async function stop() {
    if (!token || !sessionId) return;
    setLoading(true);
    const result = await streamingApi.stopPlayback(token, sessionId);
    setLastAction(result);
    setLoading(false);
  }

  async function updateProgress() {
    if (!token || !sessionId) return;
    setLoading(true);
    const result = await streamingApi.updateProgress(token, sessionId, position);
    setLastAction(result);
    setLoading(false);
  }

  return (
    <div className="page">
      <PageHeader
        title="Playback"
        description="Start, progress, and stop streaming sessions. Requires an active subscription (billing service)."
        service="streaming-service"
        port={8083}
      />

      <div className="two-col">
        <section className="panel">
          <Field label="Content ID">
            <select value={contentId} onChange={(e) => setContentId(e.target.value)}>
              {SEED_CONTENT_IDS.map((id) => (
                <option key={id} value={id}>{id}</option>
              ))}
            </select>
          </Field>
          <div className="btn-row">
            <ActionButton label="Start playback" onClick={start} disabled={loading} />
            <ActionButton label="My sessions" onClick={loadSessions} variant="secondary" disabled={loading} />
          </div>
          <ResponsePanel title="Sessions" result={sessions} loading={loading} />
        </section>

        <section className="panel">
          <Field label="Session ID">
            <input value={sessionId} onChange={(e) => setSessionId(e.target.value)} />
          </Field>
          <Field label="Position (seconds)">
            <input
              type="number"
              value={position}
              onChange={(e) => setPosition(Number(e.target.value))}
            />
          </Field>
          <div className="btn-row">
            <ActionButton label="Update progress" onClick={updateProgress} disabled={loading} />
            <ActionButton label="Stop session" onClick={stop} variant="danger" disabled={loading} />
          </div>
          <ResponsePanel title="Last action" result={lastAction} loading={loading} />
        </section>
      </div>
    </div>
  );
}
