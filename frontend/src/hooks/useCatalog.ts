import { useCallback, useEffect, useState } from "react";
import * as catalogApi from "@/api/catalog";
import type { ContentItem } from "@/types";

export function useCatalog(search?: string) {
  const [items, setItems] = useState<ContentItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(async () => {
    setLoading(true);
    setError(null);
    const result = await catalogApi.searchContent(search || undefined);
    if (result.ok && result.data) {
      setItems(result.data.searchContent.items);
    } else {
      setError(result.error ?? "Could not load catalog");
    }
    setLoading(false);
  }, [search]);

  useEffect(() => {
    void reload();
  }, [reload]);

  return { items, loading, error, reload };
}

export function contentTitle(items: ContentItem[], id: string): string {
  return items.find((item) => item.id === id)?.title ?? "Selected title";
}
