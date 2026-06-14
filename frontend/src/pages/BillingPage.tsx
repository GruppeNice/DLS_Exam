import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import * as billingApi from "@/api/billing";
import type { ApiResult, Plan } from "@/types";
import type { Subscription } from "@/api/billing";
import { ActionButton, StatusMessage, StreamPageHeader } from "@/components/ui";

export function BillingPage() {
  const { token, user } = useAuth();
  const [plans, setPlans] = useState<Plan[]>([]);
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
  const [active, setActive] = useState(false);
  const [activePlanCode, setActivePlanCode] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function loadAll() {
    setLoading(true);
    setError(null);
    const [plansResult, subsResult] = await Promise.all([
      billingApi.getPlans(),
      token ? billingApi.getMySubscriptions(token) : Promise.resolve({ ok: false, status: 0 } as ApiResult<Subscription[]>),
    ]);

    if (plansResult.ok && plansResult.data) setPlans(plansResult.data);
    if (subsResult.ok && subsResult.data) setSubscriptions(subsResult.data);

    if (user) {
      const activeResult = await billingApi.getActiveSubscription(user.id);
      if (activeResult.ok && activeResult.data) {
        setActive(activeResult.data.active);
        setActivePlanCode(activeResult.data.subscription?.planCode ?? null);
      }
    }
    setLoading(false);
  }

  async function subscribe(plan: Plan) {
    if (!token) return;
    if (active) {
      setError("You already have an active plan. Cancel it first or keep watching.");
      return;
    }

    setLoading(true);
    setError(null);
    setMessage(null);
    const result = await billingApi.activateSubscription(token, plan.id);
    if (result.ok) {
      setMessage(`Welcome to ${plan.name}! You can start watching now.`);
      await loadAll();
    } else if (result.status === 409) {
      setError("You already have an active subscription.");
      await loadAll();
    } else {
      setError(result.error ?? `Subscribe failed (${result.status})`);
    }
    setLoading(false);
  }

  useEffect(() => {
    void loadAll();
  }, [user, token]);

  return (
    <div className="page">
      <StreamPageHeader
        title="My Plan"
        description="Subscriptions gate playback. When you activate a plan, billing publishes events that engagement and streaming services consume."
      />

      {active && (
        <StatusMessage variant="success">
          Active plan: <strong>{activePlanCode}</strong>.{" "}
          <Link to="/playback">Start watching →</Link>
        </StatusMessage>
      )}
      {message && <StatusMessage variant="success">{message}</StatusMessage>}
      {error && <StatusMessage variant="error">{error}</StatusMessage>}

      <div className="plan-grid">
        {plans.map((plan) => {
          const isCurrent = active && subscriptions.some(
            (sub) => sub.planId === plan.id && sub.status === "ACTIVE",
          );
          return (
            <article key={plan.id} className={`plan-card ${isCurrent ? "plan-current" : ""}`}>
              {isCurrent && <span className="plan-ribbon">Current plan</span>}
              <p className="eyebrow">{plan.code}</p>
              <h3>{plan.name}</h3>
              <p className="price">
                ${(plan.priceCents / 100).toFixed(2)}
                <span> / {plan.billingPeriodDays} days</span>
              </p>
              <p className="muted">{plan.description}</p>
              <ActionButton
                label={isCurrent ? "Active" : active ? "Switch later" : "Subscribe"}
                onClick={() => subscribe(plan)}
                disabled={loading || isCurrent || (active && !isCurrent)}
              />
            </article>
          );
        })}
        {plans.length === 0 && (
          <p className="muted">No plans loaded. Is billing-service running?</p>
        )}
      </div>

      {subscriptions.length > 0 && (
        <section className="panel">
          <h2>Subscription history</h2>
          <ul className="sub-history">
            {subscriptions.map((sub) => (
              <li key={sub.id}>
                <strong>{sub.planName}</strong> — {sub.status}
                <span className="muted"> since {new Date(sub.startedAt).toLocaleDateString()}</span>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  );
}
