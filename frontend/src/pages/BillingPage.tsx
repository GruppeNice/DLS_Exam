import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import * as billingApi from "@/api/billing";
import type { ApiResult, Plan } from "@/types";
import { SEED_PLAN_IDS } from "@/types";
import { ActionButton, PageHeader, ResponsePanel } from "@/components/ui";

export function BillingPage() {
  const { token, user } = useAuth();
  const [plans, setPlans] = useState<Plan[]>([]);
  const [subscriptions, setSubscriptions] = useState<ApiResult<unknown> | null>(null);
  const [active, setActive] = useState<ApiResult<unknown> | null>(null);
  const [lastAction, setLastAction] = useState<ApiResult<unknown> | null>(null);
  const [loading, setLoading] = useState(false);

  async function loadPlans() {
    setLoading(true);
    const result = await billingApi.getPlans();
    if (result.ok && result.data) setPlans(result.data);
    setLoading(false);
  }

  async function loadSubscriptions() {
    if (!token) return;
    setLoading(true);
    const result = await billingApi.getMySubscriptions(token);
    setSubscriptions(result);
    setLoading(false);
  }

  async function loadActive() {
    if (!user) return;
    setLoading(true);
    const result = await billingApi.getActiveSubscription(user.id);
    setActive(result);
    setLoading(false);
  }

  async function subscribe(planId: string) {
    if (!token) return;
    setLoading(true);
    const result = await billingApi.activateSubscription(token, planId);
    setLastAction(result);
    await loadSubscriptions();
    await loadActive();
    setLoading(false);
  }

  async function pay(planId: string) {
    if (!token) return;
    setLoading(true);
    const result = await billingApi.processPayment(token, planId);
    setLastAction(result);
    setLoading(false);
  }

  useEffect(() => {
    void loadPlans();
    void loadActive();
  }, [user]);

  return (
    <div className="page">
      <PageHeader
        title="Billing"
        description="View plans, activate subscriptions, and process payments. Streaming checks active subscription before playback."
        service="billing-service"
        port={8084}
      />

      <section className="panel">
        <div className="btn-row">
          <ActionButton label="Refresh plans" onClick={loadPlans} variant="secondary" disabled={loading} />
          <ActionButton label="My subscriptions" onClick={loadSubscriptions} disabled={loading} />
          <ActionButton label="Active status" onClick={loadActive} variant="secondary" disabled={loading} />
        </div>
      </section>

      <div className="plan-grid">
        {plans.map((plan) => (
          <article key={plan.id} className="plan-card">
            <p className="eyebrow">{plan.code}</p>
            <h3>{plan.name}</h3>
            <p className="price">
              ${(plan.priceCents / 100).toFixed(2)}
              <span> / {plan.billingPeriodDays}d</span>
            </p>
            <p className="muted">{plan.description}</p>
            <div className="btn-row">
              <ActionButton label="Subscribe" onClick={() => subscribe(plan.id)} disabled={loading} />
              <ActionButton label="Pay" onClick={() => pay(plan.id)} variant="secondary" disabled={loading} />
            </div>
          </article>
        ))}
        {plans.length === 0 && (
          <p className="muted">No plans loaded. Is billing-service running on :8084?</p>
        )}
      </div>

      <div className="two-col">
        <ResponsePanel title="My subscriptions" result={subscriptions} loading={loading} />
        <ResponsePanel title="Active / last action" result={lastAction ?? active} loading={loading} />
      </div>

      <section className="panel muted-box">
        <p>Seeded plan IDs for quick reference:</p>
        <ul>
          {Object.entries(SEED_PLAN_IDS).map(([code, id]) => (
            <li key={code}><strong>{code}</strong>: <code>{id}</code></li>
          ))}
        </ul>
      </section>
    </div>
  );
}
