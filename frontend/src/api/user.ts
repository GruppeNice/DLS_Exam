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

export interface GoogleOAuthStatus {
  enabled: boolean;
  provider: string;
  authorizationPath?: string;
  callbackPath?: string;
}

export async function getGoogleOAuthStatus(): Promise<ApiResult<GoogleOAuthStatus>> {
  return request(`${API.user}/api/v1/oauth/google/status`);
}

export function googleOAuthAuthorizationUrl(userServiceBase = "http://localhost:8081"): string {
  return `${userServiceBase}/oauth2/authorization/google`;
}

export async function completeOAuth(token: string): Promise<ApiResult<AuthResponse>> {
  const me = await getMe(token);
  if (!me.ok || !me.data) {
    return { ok: false, status: me.status, error: me.error ?? "Could not load profile" };
  }
  return {
    ok: true,
    status: 200,
    data: {
      accessToken: token,
      tokenType: "Bearer",
      expiresInSeconds: 3600,
      user: me.data,
    },
  };
}

export async function checkHealth(): Promise<ApiResult<unknown>> {
  return request(`${API.user}/actuator/health`);
}
