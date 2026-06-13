export const API = {
  user: "/api/user",
  catalog: "/api/catalog",
  streaming: "/api/streaming",
  billing: "/api/billing",
  review: "/api/review",
  engagement: "/api/engagement",
  recommendation: "/api/recommendation",
} as const;

export const SEED_CONTENT_IDS = [
  "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1",
  "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2",
  "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3",
] as const;

export const SEED_PLAN_IDS = {
  BASIC: "11111111-1111-1111-1111-111111111101",
  PREMIUM: "11111111-1111-1111-1111-111111111102",
  FAMILY: "11111111-1111-1111-1111-111111111103",
} as const;

export interface UserProfile {
  id: string;
  email: string;
  displayName: string;
  status: string;
  roles: string[];
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: UserProfile;
}

export interface ContentItem {
  id: string;
  title: string;
  description?: string;
  contentType: string;
  releaseDate?: string;
  durationMinutes?: number;
  posterUrl?: string;
  ratingStats?: {
    averageRating: number;
    ratingCount: number;
    reviewCount: number;
  };
}

export interface Plan {
  id: string;
  code: string;
  name: string;
  description: string;
  priceCents: number;
  currency: string;
  billingPeriodDays: number;
  active: boolean;
}

export interface PlaybackSession {
  id: string;
  userId: string;
  contentId: string;
  status: string;
  positionSeconds: number;
  drmToken?: string;
  startedAt: string;
  stoppedAt?: string | null;
  resumedAt?: string | null;
  updatedAt: string;
}

export interface RecommendationItem {
  content_id: string;
  score: number;
  reason: string;
}

export interface ApiResult<T> {
  ok: boolean;
  status: number;
  data?: T;
  error?: string;
}
