import { API } from "@/types";
import type { ApiResult } from "@/types";
import { jsonHeaders, request } from "./client";

export interface NotificationRequest {
  type: "EMAIL" | "SMS" | "PUSH";
  recipient: string;
  subject: string;
  templateName: string;
  templateVariables?: Record<string, string>;
}

export interface Notification {
  id: number;
  type: string;
  recipient: string;
  subject: string;
  content: string;
  status: string;
  retryCount: number;
  createdAt: string;
  sentAt?: string | null;
}

export async function sendNotification(
  payload: NotificationRequest,
): Promise<ApiResult<{ status: string; message: string; notificationId: number }>> {
  return request(`${API.engagement}/api/notifications`, {
    method: "POST",
    headers: jsonHeaders(null),
    body: JSON.stringify(payload),
  });
}

export async function getNotification(
  id: number,
): Promise<ApiResult<Notification>> {
  return request(`${API.engagement}/api/notifications/${id}`);
}

export async function checkHealth(): Promise<ApiResult<unknown>> {
  const result = await request(`${API.engagement}/api/notifications/0`);
  return result.status > 0
    ? { ok: true, status: result.status, data: { status: "reachable" } }
    : result;
}
