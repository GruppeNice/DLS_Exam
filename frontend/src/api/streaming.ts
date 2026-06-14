import { API } from "@/types";
import type { ApiResult, PlaybackSession } from "@/types";
import { authHeaders, idempotencyKey, jsonHeaders, request } from "./client";

export async function getMySessions(
  token: string,
): Promise<ApiResult<PlaybackSession[]>> {
  return request(`${API.streaming}/api/v1/playback/sessions/me`, {
    headers: authHeaders(token),
  });
}

export async function getLatestSessionForContent(
  token: string,
  contentId: string,
): Promise<ApiResult<PlaybackSession>> {
  return request(`${API.streaming}/api/v1/playback/sessions/content/${contentId}`, {
    headers: authHeaders(token),
  });
}

export async function startPlayback(
  token: string,
  contentId: string,
): Promise<ApiResult<PlaybackSession>> {
  return request(`${API.streaming}/api/v1/playback/start`, {
    method: "POST",
    headers: {
      ...jsonHeaders(token),
      "Idempotency-Key": idempotencyKey(),
    },
    body: JSON.stringify({ contentId }),
  });
}

export async function resumePlayback(
  token: string,
  sessionId: string,
): Promise<ApiResult<PlaybackSession>> {
  return request(`${API.streaming}/api/v1/playback/sessions/${sessionId}/resume`, {
    method: "POST",
    headers: authHeaders(token),
  });
}

export async function stopPlayback(
  token: string,
  sessionId: string,
  positionSeconds?: number,
): Promise<ApiResult<PlaybackSession>> {
  return request(`${API.streaming}/api/v1/playback/sessions/${sessionId}/stop`, {
    method: "POST",
    headers: jsonHeaders(token),
    body: JSON.stringify(
      positionSeconds === undefined ? {} : { positionSeconds },
    ),
  });
}

export async function updateProgress(
  token: string,
  sessionId: string,
  positionSeconds: number,
): Promise<ApiResult<PlaybackSession>> {
  return request(`${API.streaming}/api/v1/playback/sessions/${sessionId}/progress`, {
    method: "PUT",
    headers: jsonHeaders(token),
    body: JSON.stringify({ positionSeconds }),
  });
}

export async function checkHealth(): Promise<ApiResult<unknown>> {
  return request(`${API.streaming}/actuator/health`);
}
