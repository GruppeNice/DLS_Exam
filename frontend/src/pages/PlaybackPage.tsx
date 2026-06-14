import { useCallback, useEffect, useRef, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import * as billingApi from "@/api/billing";
import * as streamingApi from "@/api/streaming";
import { useCatalog, contentTitle } from "@/hooks/useCatalog";
import type { PlaybackSession } from "@/types";
import { SEED_CONTENT_IDS } from "@/types";
import { ActionButton, StatusMessage, StreamPageHeader } from "@/components/ui";

type PlayerState = "idle" | "playing" | "paused" | "stopped";

function formatTime(seconds: number): string {
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins}:${secs.toString().padStart(2, "0")}`;
}

export function PlaybackPage() {
  const { token, user } = useAuth();
  const [searchParams] = useSearchParams();
  const { items, loading: catalogLoading } = useCatalog();
  const initialContent = searchParams.get("content") ?? SEED_CONTENT_IDS[0];

  const [contentId, setContentId] = useState(initialContent);
  const [sessionId, setSessionId] = useState("");
  const [position, setPosition] = useState(0);
  const [duration, setDuration] = useState(7200);
  const [playerState, setPlayerState] = useState<PlayerState>("idle");
  const [savedSession, setSavedSession] = useState<PlaybackSession | null>(null);
  const [allSessions, setAllSessions] = useState<PlaybackSession[]>([]);
  const [subscriptionActive, setSubscriptionActive] = useState<boolean | null>(null);
  const [status, setStatus] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [lastSession, setLastSession] = useState<PlaybackSession | null>(null);

  const positionRef = useRef(position);
  positionRef.current = position;

  const selected = items.find((item) => item.id === contentId);

  const loadSavedProgress = useCallback(async (forContent: string) => {
    if (!token) return;
    const result = await streamingApi.getLatestSessionForContent(token, forContent);
    if (result.ok && result.data) {
      const session = result.data;
      setSavedSession(session);
      setSessionId(session.id);
      setPosition(session.positionSeconds ?? 0);
      setLastSession(session);
      if (session.status === "ACTIVE") {
        setPlayerState("paused");
        setStatus(`Paused at ${formatTime(session.positionSeconds ?? 0)} — press play to continue`);
      } else if (session.status === "STOPPED" && (session.positionSeconds ?? 0) > 0) {
        setPlayerState("stopped");
        setStatus(`Pick up where you left off at ${formatTime(session.positionSeconds ?? 0)}`);
      } else {
        setPlayerState("idle");
        setStatus(null);
      }
    } else if (result.status === 401 || result.status === 403) {
      setSavedSession(null);
      setSessionId("");
      setPosition(0);
      setPlayerState("idle");
      setStatus(null);
      setError(result.error ?? "Your session has expired. Please log out and sign in again.");
    } else {
      setSavedSession(null);
      setSessionId("");
      setPosition(0);
      setPlayerState("idle");
      setStatus(null);
    }
  }, [token]);

  const loadAllSessions = useCallback(async () => {
    if (!token) return;
    const result = await streamingApi.getMySessions(token);
    if (result.ok && result.data) {
      setAllSessions(result.data);
    }
  }, [token]);

  useEffect(() => {
    const fromUrl = searchParams.get("content");
    if (fromUrl) setContentId(fromUrl);
  }, [searchParams]);

  useEffect(() => {
    if (selected?.durationMinutes) {
      setDuration(selected.durationMinutes * 60);
    }
  }, [selected]);

  useEffect(() => {
    if (!user) return;
    void billingApi.getActiveSubscription(user.id).then((result) => {
      setSubscriptionActive(result.ok ? Boolean(result.data?.active) : false);
    });
  }, [user]);

  useEffect(() => {
    if (!token || !contentId) return;
    void loadSavedProgress(contentId);
    void loadAllSessions();
  }, [token, contentId, loadSavedProgress, loadAllSessions]);

  useEffect(() => {
    if (playerState !== "playing" || !sessionId || !token) return;

    const tick = window.setInterval(() => {
      setPosition((prev) => Math.min(prev + 1, duration));
    }, 1000);

    const sync = window.setInterval(() => {
      void streamingApi.updateProgress(token, sessionId, positionRef.current).then((result) => {
        if (result.ok && result.data) setLastSession(result.data);
      });
    }, 10000);

    return () => {
      window.clearInterval(tick);
      window.clearInterval(sync);
    };
  }, [playerState, sessionId, token, duration]);

  async function syncProgress(nextPosition: number) {
    if (!token || !sessionId) return;
    const result = await streamingApi.updateProgress(token, sessionId, nextPosition);
    if (result.ok && result.data) {
      setLastSession(result.data);
      setSavedSession(result.data);
    }
  }

  async function startOrResume() {
    if (!token) return;
    if (!subscriptionActive) {
      setError("You need an active plan before watching. Head to My Plan to subscribe.");
      return;
    }

    setLoading(true);
    setError(null);

    if (savedSession?.status === "STOPPED" && sessionId) {
      const result = await streamingApi.resumePlayback(token, sessionId);
      if (result.ok && result.data) {
        setLastSession(result.data);
        setSavedSession(result.data);
        setPosition(result.data.positionSeconds ?? position);
        setPlayerState("playing");
        setStatus(`Resumed ${contentTitle(items, contentId)} at ${formatTime(result.data.positionSeconds ?? 0)}`);
      } else {
        setError(result.error ?? "Could not resume playback");
      }
      setLoading(false);
      return;
    }

    if (savedSession?.status === "ACTIVE" && sessionId) {
      setPlayerState("playing");
      setStatus(`Continuing ${contentTitle(items, contentId)}`);
      setLoading(false);
      return;
    }

    const result = await streamingApi.startPlayback(token, contentId);
    if (result.ok && result.data) {
      setSessionId(result.data.id);
      setLastSession(result.data);
      setSavedSession(result.data);
      setPosition(result.data.positionSeconds ?? 0);
      setPlayerState("playing");
      const resumed = (result.data.positionSeconds ?? 0) > 0;
      setStatus(
        resumed
          ? `Resumed ${contentTitle(items, contentId)} at ${formatTime(result.data.positionSeconds ?? 0)}`
          : `Now playing ${contentTitle(items, contentId)}`,
      );
      void loadAllSessions();
    } else {
      setError(result.error ?? "Could not start playback");
      setStatus(null);
    }
    setLoading(false);
  }

  async function stop() {
    if (!token || !sessionId) return;
    setLoading(true);
    setError(null);
    const result = await streamingApi.stopPlayback(token, sessionId, positionRef.current);
    if (result.ok && result.data) {
      setLastSession(result.data);
      setSavedSession(result.data);
      setPosition(result.data.positionSeconds ?? position);
      setPlayerState("stopped");
      setStatus(
        `Stopped at ${formatTime(result.data.positionSeconds ?? 0)} — progress saved for next time`,
      );
      void loadAllSessions();
    } else {
      setError(result.error ?? "Could not stop playback");
    }
    setLoading(false);
  }

  function handleScrub(value: number) {
    setPosition(value);
    if (playerState === "playing" || playerState === "paused") {
      void syncProgress(value);
    }
  }

  async function togglePlay() {
    if (playerState === "playing") {
      await syncProgress(positionRef.current);
      setPlayerState("paused");
      setStatus(`Paused at ${formatTime(positionRef.current)}`);
      return;
    }
    if (playerState === "paused" && sessionId) {
      setPlayerState("playing");
      setStatus(`Playing ${contentTitle(items, contentId)}`);
      return;
    }
    await startOrResume();
  }

  function sessionProgressFor(content: string): number | null {
    const session = allSessions.find((s) => s.contentId === content);
    if (!session || !session.positionSeconds) return null;
    const item = items.find((i) => i.id === content);
    const total = (item?.durationMinutes ?? 120) * 60;
    return Math.round((session.positionSeconds / total) * 100);
  }

  return (
    <div className="page watch-page">
      <StreamPageHeader
        title="Watch"
        description="PlaybackStarted, PlaybackProgressUpdated, and PlaybackStopped keep your place — like picking up John Wick 3 where you left off."
      />

      {subscriptionActive === false && (
        <StatusMessage variant="warning">
          No active subscription. <Link to="/billing">Choose a plan</Link> to unlock playback.
        </StatusMessage>
      )}

      {savedSession?.status === "STOPPED" && position > 0 && playerState !== "playing" && (
        <StatusMessage variant="success">
          Continue watching from {formatTime(position)}
        </StatusMessage>
      )}

      <section className="player-stage">
        <div className="player-screen">
          <div className="player-backdrop">
            {selected?.posterUrl ? (
              <img src={selected.posterUrl} alt="" className="player-backdrop-img" />
            ) : (
              <div className="player-backdrop-fallback">
                {selected?.title?.[0] ?? "▶"}
              </div>
            )}
            <div className="player-overlay" />
          </div>

          <div className="player-ui">
            <div className="player-top">
              <span className="player-badge">
                {playerState === "playing" ? "PLAYING" : playerState.toUpperCase()}
              </span>
              {position > 0 && playerState !== "idle" && (
                <span className="player-session">{formatTime(position)} / {formatTime(duration)}</span>
              )}
            </div>

            <div className="player-center">
              <button
                type="button"
                className="player-play-btn"
                onClick={() => void togglePlay()}
                disabled={loading || catalogLoading}
                aria-label={playerState === "playing" ? "Pause" : "Play"}
              >
                {playerState === "playing" ? "❚❚" : "▶"}
              </button>
            </div>

            <div className="player-bottom">
              <div className="player-meta">
                <h2>{selected?.title ?? contentTitle(items, contentId)}</h2>
                <p>{selected?.description ?? "Pick a title from the row below or browse the catalog."}</p>
              </div>
              <div className="player-controls">
                <span className="time-label">{formatTime(position)}</span>
                <input
                  type="range"
                  min={0}
                  max={duration}
                  value={position}
                  className="progress-slider"
                  onChange={(e) => handleScrub(Number(e.target.value))}
                  disabled={!sessionId && playerState === "idle"}
                />
                <span className="time-label">{formatTime(duration)}</span>
              </div>
            </div>
          </div>
        </div>

        <aside className="player-sidebar">
          <h3>Red thread</h3>
          <ol className="journey-steps">
            <li className={subscriptionActive ? "done" : ""}>Billing verifies subscription</li>
            <li className={sessionId ? "done" : ""}>PlaybackStarted opens or resumes a session</li>
            <li className={position > 0 ? "done" : ""}>PlaybackProgressUpdated while you watch</li>
            <li className={playerState === "stopped" ? "done" : ""}>PlaybackStopped saves your place</li>
            <li>Review & recommendations pick up from here</li>
          </ol>
          <div className="btn-row">
            <ActionButton
              label="Stop & save progress"
              onClick={stop}
              variant="danger"
              disabled={loading || !sessionId || playerState === "stopped"}
            />
            <Link to="/catalog" className="btn btn-secondary">Browse catalog</Link>
          </div>
          {status && <StatusMessage variant="success">{status}</StatusMessage>}
          {error && <StatusMessage variant="error">{error}</StatusMessage>}
        </aside>
      </section>

      <section className="panel continue-watching">
        <div className="section-head">
          <h2>Continue watching</h2>
          <Link to="/catalog" className="text-link">View all titles</Link>
        </div>
        <div className="title-rail">
          {items.map((item) => {
            const pct = sessionProgressFor(item.id);
            return (
              <button
                key={item.id}
                type="button"
                className={`title-chip ${contentId === item.id ? "active" : ""}`}
                onClick={() => {
                  setContentId(item.id);
                  setError(null);
                }}
              >
                <span className="chip-letter">{item.title[0]}</span>
                <span className="chip-label">
                  {item.title}
                  {pct != null && pct > 0 ? ` · ${pct}%` : ""}
                </span>
              </button>
            );
          })}
        </div>
      </section>

      {lastSession && (
        <details className="dev-panel">
          <summary>Technical session details</summary>
          <pre>{JSON.stringify(lastSession, null, 2)}</pre>
        </details>
      )}
    </div>
  );
}
