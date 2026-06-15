import type { ContentItem, RecommendationItem } from "@/types";

const REASON_LABELS: Record<string, string> = {
  trending_aggregate: "Popular across the platform",
  precomputed_nmf: "Picked for you by the ML model",
  nmf_rewatch: "Top match from your taste profile",
};

const MODEL_LABELS: Record<string, string> = {
  nmf_collaborative_filtering: "Collaborative filtering (NMF)",
  awaiting_retrain: "Retrain required",
};

export function recommendationReasonLabel(reason: string): string {
  return REASON_LABELS[reason] ?? reason.replaceAll("_", " ");
}

export function recommendationModelLabel(modelType?: string): string {
  if (!modelType) return "";
  return MODEL_LABELS[modelType] ?? modelType.replaceAll("_", " ");
}

export function formatRecommendationScore(item: RecommendationItem): string {
  if (item.reason === "precomputed_nmf" || item.reason === "nmf_rewatch") {
    return "Model match";
  }
  return `Popularity ${item.score.toFixed(1)}`;
}

export function contentLookup(
  catalog: ContentItem[],
  contentId: string,
): ContentItem | undefined {
  return catalog.find((item) => item.id === contentId);
}

export function contentTitleOrId(catalog: ContentItem[], contentId: string): string {
  return contentLookup(catalog, contentId)?.title ?? contentId;
}
