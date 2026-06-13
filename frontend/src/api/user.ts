import { API } from "@/types";
import type { ApiResult, AuthResponse, UserProfile } from "@/types";
import { jsonHeaders, request } from "./client";

export async function register(
  email: string,
  password: string,
  displayName: string,
): Promise<ApiResult<AuthResponse>> {
  return request(`${API.user}/api/v1/auth/register`, {
    method: "POST",
    headers: jsonHeaders(null),
    body: JSON.stringify({ email, password, displayName }),
  });
}

export async function login(
  email: string,
  password: string,
): Promise<ApiResult<AuthResponse>> {
  return request(`${API.user}/api/v1/auth/login`, {
    method: "POST",
    headers: jsonHeaders(null),
    body: JSON.stringify({ email, password }),
  });
}

export async function getMe(token: string): Promise<ApiResult<UserProfile>> {
  return request(`${API.user}/api/v1/auth/me`, {
    headers: jsonHeaders(token),
  });
}

export async function checkHealth(): Promise<ApiResult<unknown>> {
  return request(`${API.user}/actuator/health`);
}
