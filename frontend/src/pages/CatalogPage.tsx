import { useState } from "react";
import { Link } from "react-router-dom";
import { useCatalog } from "@/hooks/useCatalog";
import { ActionButton, StreamPageHeader } from "@/components/ui";

export function CatalogPage() {
  const [search, setSearch] = useState("");
  const [query, setQuery] = useState("");
  const { items, loading, error, reload } = useCatalog(query);

  function runSearch() {
    setQuery(search.trim());
  }

  return (
    <div className="page">
      <StreamPageHeader
        title="Browse"
        description="Flyway-seeded catalog across Action, Drama, Sci-Fi, Comedy, Horror, and Documentary. Pick a title and jump straight into Watch."
      />

      <section className="browse-toolbar panel">
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search titles… e.g. Neon Horizon"
          onKeyDown={(e) => e.key === "Enter" && runSearch()}
        />
        <div className="btn-row">
          <ActionButton label="Search" onClick={runSearch} disabled={loading} />
          <ActionButton label="Show all" onClick={() => { setSearch(""); setQuery(""); void reload(); }} variant="secondary" disabled={loading} />
        </div>
      </section>

      {error && <p className="form-error">{error}</p>}

      <section className="content-grid browse-grid">
        {items.map((item) => (
          <article key={item.id} className="content-card browse-card">
            <div className="content-poster">
              {item.posterUrl ? (
                <img src={item.posterUrl} alt="" />
              ) : (
                <div className="poster-fallback">{item.title[0]}</div>
              )}
              <Link to={`/playback?content=${item.id}`} className="play-overlay">
                ▶ Watch
              </Link>
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
              <p className="muted card-blurb">{item.description}</p>
              {item.ratingStats && (
                <p className="rating">
                  ★ {item.ratingStats.averageRating.toFixed(1)} ({item.ratingStats.ratingCount} ratings)
                </p>
              )}
              <Link to={`/playback?content=${item.id}`} className="watch-link">
                Watch now →
              </Link>
            </div>
          </article>
        ))}
        {!loading && items.length === 0 && (
          <p className="muted">No titles found. Is catalog-service running?</p>
        )}
      </section>
    </div>
  );
}
