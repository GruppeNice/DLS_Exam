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
      const message =
        typeof data === "object" && data && "error" in data
          ? String((data as { error: unknown }).error)
          : typeof data === "object" && data && "message" in data
            ? String((data as { message: unknown }).message)
            : `Request failed (${response.status})`;
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
