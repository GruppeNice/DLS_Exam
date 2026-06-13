import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import * as userApi from "@/api/user";
import * as catalogApi from "@/api/catalog";
import * as streamingApi from "@/api/streaming";
import * as billingApi from "@/api/billing";
import * as reviewApi from "@/api/review";
import * as engagementApi from "@/api/engagement";
import * as recommendationApi from "@/api/recommendation";
import { ActionButton, PageHeader, StatusDot } from "@/components/ui";
import { SEED_CONTENT_IDS, SEED_PLAN_IDS } from "@/types";

const SERVICES = [
  { key: "user", label: "User", port: 8081, check: userApi.checkHealth },
  { key: "catalog", label: "Catalog", port: 8082, check: catalogApi.checkHealth },
  { key: "streaming", label: "Streaming", port: 8083, check: streamingApi.checkHealth },
  { key: "billing", label: "Billing", port: 8084, check: billingApi.checkHealth },
  { key: "review", label: "Review", port: 8085, check: reviewApi.checkHealth },
  { key: "engagement", label: "Engagement", port: 8086, check: engagementApi.checkHealth },
  { key: "recommendation", label: "Recommendation", port: 8090, check: recommendationApi.checkHealth },
] as const;

export function OverviewPage() {
  const { user, token } = useAuth();
  const [health, setHealth] = useState<Record<string, boolean>>({});
  const [flowLog, setFlowLog] = useState<string[]>([]);
  const [flowRunning, setFlowRunning] = useState(false);

  const refreshHealth = useCallback(async () => {
    const results = await Promise.all(
      SERVICES.map(async (s) => {
        const r = await s.check();
        return [s.key, r.ok || r.status > 0] as const;
      }),
    );
    setHealth(Object.fromEntries(results));
  }, []);

  useEffect(() => {
    void refreshHealth();
  }, [refreshHealth]);

  async function runDemoFlow() {
    if (!token || !user) return;
    setFlowRunning(true);
    const log: string[] = [];

    const sub = await billingApi.getActiveSubscription(user.id);
    log.push(sub.data?.active ? "✓ Active subscription found" : "→ No subscription yet");

    if (!sub.data?.active) {
      const activated = await billingApi.activateSubscription(token, SEED_PLAN_IDS.BASIC);
      log.push(activated.ok ? "✓ Subscribed to Basic plan" : `✗ Subscribe failed: ${activated.error}`);
    }

    const contentId = SEED_CONTENT_IDS[0];
    const playback = await streamingApi.startPlayback(token, contentId);
    log.push(playback.ok ? `✓ Playback started for ${contentId}` : `✗ Playback failed: ${playback.error}`);

    if (playback.ok && playback.data) {
      await streamingApi.updateProgress(token, playback.data.id, 120);
      log.push("✓ Progress updated to 120s");
      await streamingApi.stopPlayback(token, playback.data.id);
      log.push("✓ Playback stopped");
    }

    await reviewApi.addRating(user.id, contentId, 5);
    log.push("✓ Rating submitted (review service)");

    await reviewApi.addReview(user.id, contentId, "Loved it — great test run!", false);
    log.push("✓ Review submitted");

    await recommendationApi.ingestInteraction(user.id, contentId);
    log.push("✓ Interaction ingested (recommendation service)");

    const notif = await engagementApi.sendNotification({
      type: "EMAIL",
      recipient: user.email,
      subject: "Thanks for watching!",
      templateName: "watch-complete",
      templateVariables: { name: user.displayName },
    });
    log.push(notif.ok ? `✓ Notification queued (#${notif.data?.notificationId})` : `✗ Notification failed: ${notif.error ?? notif.status}`);

    const recs = await recommendationApi.getMyRecommendations(token);
    log.push(recs.ok ? `✓ Got ${recs.data?.items.length ?? 0} recommendations` : `✗ Recommendations: ${recs.error}`);

    setFlowLog(log);
    setFlowRunning(false);
    void refreshHealth();
  }

  return (
    <div className="page">
      <PageHeader
        title="Service overview"
        description="Health checks and a guided flow across user → billing → streaming → review → engagement → recommendation."
        service="Platform"
        port={3000}
      />

      <section className="card-grid">
        {SERVICES.map((s) => (
          <div key={s.key} className="card">
            <StatusDot up={health[s.key] ?? false} label={s.label} />
            <p className="card-meta">localhost:{s.port}</p>
          </div>
        ))}
      </section>

      <div className="two-col">
        <section className="panel">
          <h2>Your session</h2>
          <dl className="kv">
            <dt>User ID</dt>
            <dd><code>{user?.id}</code></dd>
            <dt>Email</dt>
            <dd>{user?.email}</dd>
            <dt>Roles</dt>
            <dd>{user?.roles.join(", ")}</dd>
          </dl>
          <ActionButton label="Refresh health" onClick={refreshHealth} variant="secondary" />
        </section>

        <section className="panel">
          <h2>End-to-end demo flow</h2>
          <p className="muted">
            Activates a plan if needed, plays seeded content, rates it, ingests an interaction,
            queues an email via MailHog, and fetches recommendations.
          </p>
          <ActionButton
            label={flowRunning ? "Running…" : "Run demo flow"}
            onClick={runDemoFlow}
            disabled={flowRunning}
          />
          {flowLog.length > 0 && (
            <ul className="flow-log">
              {flowLog.map((line) => (
                <li key={line}>{line}</li>
              ))}
            </ul>
          )}
        </section>
      </div>

      <section className="panel">
        <h2>Quick links</h2>
        <div className="link-row">
          <Link to="/catalog">Browse catalog</Link>
          <Link to="/playback">Playback sessions</Link>
          <Link to="/billing">Plans & subscriptions</Link>
          <Link to="/reviews">Reviews & ratings</Link>
          <Link to="/notifications">Send notification</Link>
          <Link to="/recommendations">Recommendations</Link>
          <a href="http://localhost:8025" target="_blank" rel="noreferrer">MailHog UI</a>
          <a href="http://localhost:15672" target="_blank" rel="noreferrer">RabbitMQ UI</a>
        </div>
      </section>
    </div>
  );
}
