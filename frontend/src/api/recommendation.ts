import { API } from "@/types";
import type { ApiResult, RecommendationItem } from "@/types";
import { authHeaders, jsonHeaders, request } from "./client";

export async function getMyRecommendations(
  token: string,
): Promise<ApiResult<{ user_id: string; items: RecommendationItem[]; generated_at: string; model_type: string }>> {
  return request(`${API.recommendation}/api/v1/recommendations/me`, {
    headers: authHeaders(token),
  });
}

export async function getTrending(): Promise<
  ApiResult<{ items: RecommendationItem[]; generated_at: string }>
> {
  return request(`${API.recommendation}/api/v1/recommendations/trending`);
}

export async function ingestInteraction(
  userId: string,
  contentId: string,
  weight = 1.0,
  sourceEvent = "playback.completed",
): Promise<ApiResult<{ status: string }>> {
  return request(`${API.recommendation}/api/v1/recommendations/interactions`, {
    method: "POST",
    headers: jsonHeaders(null),
    body: JSON.stringify({
      user_id: userId,
      content_id: contentId,
      weight,
      source_event: sourceEvent,
    }),
  });
}

export async function retrain(): Promise<ApiResult<unknown>> {
  return request(`${API.recommendation}/api/v1/recommendations/retrain`, {
    method: "POST",
  });
}

export async function checkHealth(): Promise<ApiResult<{ status: string; service: string }>> {
  return request(`${API.recommendation}/api/v1/health`);
}
