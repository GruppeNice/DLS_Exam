import { API } from "@/types";
import type { ApiResult, Plan } from "@/types";
import { authHeaders, idempotencyKey, jsonHeaders, request } from "./client";

export interface Subscription {
  id: string;
  userId: string;
  planId: string;
  planCode: string;
  planName: string;
  status: string;
  startedAt: string;
  endsAt?: string;
  cancelledAt?: string | null;
}

export async function getPlans(): Promise<ApiResult<Plan[]>> {
  return request(`${API.billing}/api/v1/plans`);
}

export async function getMySubscriptions(
  token: string,
): Promise<ApiResult<Subscription[]>> {
  return request(`${API.billing}/api/v1/subscriptions/me`, {
    headers: authHeaders(token),
  });
}

export async function activateSubscription(
  token: string,
  planId: string,
): Promise<ApiResult<Subscription>> {
  return request(`${API.billing}/api/v1/subscriptions`, {
    method: "POST",
    headers: {
      ...jsonHeaders(token),
      "Idempotency-Key": idempotencyKey(),
    },
    body: JSON.stringify({ planId }),
  });
}

export async function processPayment(
  token: string,
  planId: string,
): Promise<ApiResult<unknown>> {
  return request(`${API.billing}/api/v1/payments`, {
    method: "POST",
    headers: jsonHeaders(token),
    body: JSON.stringify({ planId, idempotencyKey: idempotencyKey() }),
  });
}

export async function getActiveSubscription(
  userId: string,
): Promise<ApiResult<{ userId: string; active: boolean; subscription?: Subscription }>> {
  return request(`${API.billing}/api/v1/subscriptions/active/${userId}`);
}

export async function checkHealth(): Promise<ApiResult<unknown>> {
  return request(`${API.billing}/actuator/health`);
}
