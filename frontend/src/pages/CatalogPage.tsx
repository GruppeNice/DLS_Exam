import { useEffect, useState } from "react";
import * as catalogApi from "@/api/catalog";
import type { ApiResult, ContentItem } from "@/types";
import { ActionButton, Field, PageHeader, ResponsePanel } from "@/components/ui";

export function CatalogPage() {
  const [search, setSearch] = useState("");
  const [contentId, setContentId] = useState("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");
  const [items, setItems] = useState<ContentItem[]>([]);
  const [detail, setDetail] = useState<ApiResult<{ contentById: ContentItem }> | null>(null);
  const [genres, setGenres] = useState<ApiResult<unknown> | null>(null);
  const [loading, setLoading] = useState(false);

  async function loadCatalog() {
    setLoading(true);
    const result = await catalogApi.searchContent(search || undefined);
    if (result.ok && result.data) {
      setItems(result.data.searchContent.items);
    }
    setLoading(false);
  }

  async function loadDetail() {
    setLoading(true);
    const result = await catalogApi.getContentById(contentId);
    setDetail(result);
    setLoading(false);
  }

  async function loadGenres() {
    setLoading(true);
    const result = await catalogApi.getGenres();
    setGenres(result);
    setLoading(false);
  }

  useEffect(() => {
    void loadCatalog();
  }, []);

  return (
    <div className="page">
      <PageHeader
        title="Catalog"
        description="Browse and search content via GraphQL. The catalog is Flyway-seeded with 10 titles across Action, Drama, Sci-Fi, Comedy, Horror, and Documentary."
        service="catalog-service"
        port={8082}
      />

      <div className="two-col">
        <section className="panel">
          <Field label="Search title">
            <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Neon" />
          </Field>
          <div className="btn-row">
            <ActionButton label="Search" onClick={loadCatalog} disabled={loading} />
            <ActionButton label="Load genres" onClick={loadGenres} variant="secondary" disabled={loading} />
          </div>
          <ResponsePanel title="Genres" result={genres} loading={loading} />
        </section>

        <section className="panel">
          <Field label="Content ID">
            <input value={contentId} onChange={(e) => setContentId(e.target.value)} />
          </Field>
          <ActionButton label="Fetch by ID" onClick={loadDetail} disabled={loading} />
          <ResponsePanel title="Content detail" result={detail} loading={loading} />
        </section>
      </div>

      <section className="panel">
        <h2>Catalog results ({items.length})</h2>
        <div className="content-grid">
          {items.map((item) => (
            <article key={item.id} className="content-card">
              <div className="content-poster">
                {item.posterUrl ? (
                  <img src={item.posterUrl} alt="" />
                ) : (
                  <div className="poster-fallback">{item.title[0]}</div>
                )}
              </div>
              <div className="content-body">
                <div className="content-meta">
                  <p className="content-type">{item.contentType}</p>
                  {item.genres && item.genres.length > 0 && (
                    <div className="genre-tags">
                      {item.genres.map((genre) => (
                        <span key={genre.id} className="genre-tag">
                          {genre.name}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
                <h3>{item.title}</h3>
                <p className="muted">{item.description}</p>
                {item.ratingStats && (
                  <p className="rating">
                    ★ {item.ratingStats.averageRating.toFixed(1)} ({item.ratingStats.ratingCount} ratings)
                  </p>
                )}
                <code className="content-id">{item.id}</code>
              </div>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}
