import type { ApiResult } from "@/types";

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public body?: unknown,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export async function request<T>(
  url: string,
  options: RequestInit = {},
): Promise<ApiResult<T>> {
  try {
    const response = await fetch(url, options);
    const text = await response.text();
    let data: T | undefined;
    if (text) {
      try {
        data = JSON.parse(text) as T;
      } catch {
        data = text as T;
      }
    }

    if (!response.ok) {
      const message = extractErrorMessage(response.status, data);
      return { ok: false, status: response.status, error: message, data };
    }

    return { ok: true, status: response.status, data };
  } catch (err) {
    return {
      ok: false,
      status: 0,
      error: err instanceof Error ? err.message : "Network error",
    };
  }
}

export function authHeaders(token: string | null): HeadersInit {
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export function jsonHeaders(token: string | null): HeadersInit {
  return {
    "Content-Type": "application/json",
    ...authHeaders(token),
  };
}

export function idempotencyKey(): string {
  return crypto.randomUUID();
}

function extractErrorMessage(status: number, data: unknown): string {
  if (typeof data === "object" && data) {
    if ("error" in data) return String((data as { error: unknown }).error);
    if ("detail" in data) return String((data as { detail: unknown }).detail);
    if ("message" in data) return String((data as { message: unknown }).message);
  }
  if (status === 401 || (status === 403 && !data)) {
    return "Your session has expired. Please log out and sign in again.";
  }
  return `Request failed (${status})`;
}
