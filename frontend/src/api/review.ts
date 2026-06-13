import { API } from "@/types";
import type { ApiResult } from "@/types";
import { jsonHeaders, request } from "./client";

export interface Review {
  id: string;
  userId: string;
  movieId: string;
  reviewText: string;
  spoiler: boolean;
}

export interface Rating {
  id: string;
  userId: string;
  movieId: string;
  userRating: number;
}

export async function addReview(
  userId: string,
  movieId: string,
  reviewText: string,
  spoiler = false,
): Promise<ApiResult<unknown>> {
  return request(`${API.review}/reviews/add`, {
    method: "POST",
    headers: jsonHeaders(null),
    body: JSON.stringify({ userId, movieId, reviewText, spoiler }),
  });
}

export async function addRating(
  userId: string,
  movieId: string,
  userRating: number,
): Promise<ApiResult<unknown>> {
  return request(`${API.review}/ratings/add`, {
    method: "POST",
    headers: jsonHeaders(null),
    body: JSON.stringify({ userId, movieId, userRating }),
  });
}

export async function getReview(reviewId: string): Promise<ApiResult<Review>> {
  return request(`${API.review}/reviews/${reviewId}`);
}

export async function getRating(ratingId: string): Promise<ApiResult<Rating>> {
  return request(`${API.review}/ratings/${ratingId}`);
}

export async function checkHealth(): Promise<ApiResult<unknown>> {
  const result = await request(`${API.review}/reviews/00000000-0000-0000-0000-000000000000`);
  return result.status > 0
    ? { ok: true, status: result.status, data: { status: "reachable" } }
    : result;
}
