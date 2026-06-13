import { API } from "@/types";
import type { ApiResult, ContentItem } from "@/types";
import { jsonHeaders, request } from "./client";

interface GraphqlResponse<T> {
  data?: T;
  errors?: { message: string }[];
}

async function graphql<T>(
  query: string,
  variables?: Record<string, unknown>,
  token?: string | null,
): Promise<ApiResult<T>> {
  const result = await request<GraphqlResponse<T>>(`${API.catalog}/graphql`, {
    method: "POST",
    headers: jsonHeaders(token ?? null),
    body: JSON.stringify({ query, variables }),
  });

  if (!result.ok || !result.data) {
    return result as ApiResult<T>;
  }

  if (result.data.errors?.length) {
    return {
      ok: false,
      status: result.status,
      error: result.data.errors.map((e) => e.message).join("; "),
    };
  }

  if (!result.data.data) {
    return { ok: false, status: result.status, error: "No data in GraphQL response" };
  }

  return { ok: true, status: result.status, data: result.data.data };
}

const SEARCH_QUERY = `
  query SearchContent($filter: ContentFilterInput, $page: Int, $size: Int) {
    searchContent(filter: $filter, page: $page, size: $size) {
      totalCount
      items {
        id
        title
        description
        contentType
        releaseDate
        durationMinutes
        posterUrl
        ratingStats { averageRating ratingCount reviewCount }
      }
    }
  }
`;

const CONTENT_BY_ID = `
  query ContentById($id: ID!) {
    contentById(id: $id) {
      id
      title
      description
      contentType
      releaseDate
      durationMinutes
      posterUrl
      genres { id name }
      tags { id name }
      ratingStats { averageRating ratingCount reviewCount }
    }
  }
`;

const GENRES_QUERY = `
  query Genres { genres { id name slug } }
`;

export async function searchContent(
  titleContains?: string,
  regionCode = "US",
): Promise<ApiResult<{ searchContent: { totalCount: number; items: ContentItem[] } }>> {
  return graphql(SEARCH_QUERY, {
    filter: { titleContains: titleContains || undefined, regionCode },
    page: 0,
    size: 20,
  });
}

export async function getContentById(
  id: string,
): Promise<ApiResult<{ contentById: ContentItem }>> {
  return graphql(CONTENT_BY_ID, { id });
}

export async function getGenres(): Promise<ApiResult<{ genres: { id: string; name: string; slug: string }[] }>> {
  return graphql(GENRES_QUERY);
}

export async function checkHealth(): Promise<ApiResult<unknown>> {
  return request(`${API.catalog}/actuator/health`);
}
