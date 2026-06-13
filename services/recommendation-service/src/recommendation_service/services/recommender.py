from datetime import datetime, timezone
from uuid import UUID

import numpy as np
import pandas as pd
from sklearn.decomposition import NMF
from sqlalchemy import select
from sqlalchemy.orm import Session

from recommendation_service.db.models import ModelRun, TrendingContent, UserContentInteraction, UserRecommendation
from recommendation_service.schemas.recommendations import RecommendationItem, RecommendationListResponse


class RecommenderService:
    def __init__(self, list_size: int = 10):
        self.list_size = list_size

    def get_recommendations_for_user(self, db: Session, user_id: UUID) -> RecommendationListResponse:
        cached = db.scalar(select(UserRecommendation).where(UserRecommendation.user_id == str(user_id)))
        if cached is not None:
            items = [
                RecommendationItem(content_id=UUID(content_id), score=1.0, reason="precomputed_nmf")
                for content_id in cached.content_ids[: self.list_size]
            ]
            return RecommendationListResponse(
                user_id=user_id,
                items=items,
                generated_at=cached.generated_at,
                model_type="nmf_collaborative_filtering",
            )

        live_items = self._generate_live_recommendations(db, user_id)
        return RecommendationListResponse(
            user_id=user_id,
            items=live_items,
            generated_at=datetime.now(timezone.utc),
            model_type="fallback_trending",
        )

    def retrain(self, db: Session) -> ModelRun:
        run = ModelRun(model_type="nmf_collaborative_filtering", status="RUNNING")
        db.add(run)
        db.commit()
        db.refresh(run)

        interactions = db.scalars(select(UserContentInteraction)).all()
        if len(interactions) < 3:
            run.status = "SKIPPED"
            run.details = "Not enough interaction data"
            run.completed_at = datetime.now(timezone.utc)
            db.commit()
            return run

        frame = pd.DataFrame(
            {
                "user_id": [row.user_id for row in interactions],
                "content_id": [row.content_id for row in interactions],
                "weight": [row.interaction_weight for row in interactions],
            }
        )
        matrix = frame.pivot_table(index="user_id", columns="content_id", values="weight", fill_value=0.0)
        user_ids = matrix.index.tolist()
        content_ids = matrix.columns.tolist()

        n_components = min(5, len(user_ids), len(content_ids))
        if n_components < 1:
            run.status = "SKIPPED"
            run.details = "Insufficient matrix shape"
            run.completed_at = datetime.now(timezone.utc)
            db.commit()
            return run

        model = NMF(n_components=n_components, init="nndsvda", random_state=42, max_iter=300)
        user_features = model.fit_transform(matrix.values)
        item_features = model.components_

        now = datetime.now(timezone.utc)
        for user_index, user_key in enumerate(user_ids):
            watched = set(matrix.columns[matrix.loc[user_key] > 0])
            scores = user_features[user_index] @ item_features
            ranked_indices = np.argsort(scores)[::-1]

            recommended: list[str] = []
            for idx in ranked_indices:
                content_key = content_ids[idx]
                if content_key in watched:
                    continue
                recommended.append(content_key)
                if len(recommended) >= self.list_size:
                    break

            if not recommended:
                recommended = self._fallback_content_ids(db, watched)

            existing = db.scalar(select(UserRecommendation).where(UserRecommendation.user_id == user_key))
            if existing is None:
                db.add(UserRecommendation(user_id=user_key, content_ids=recommended, generated_at=now))
            else:
                existing.content_ids = recommended
                existing.generated_at = now

        run.status = "SUCCEEDED"
        run.user_count = len(user_ids)
        run.content_count = len(content_ids)
        run.completed_at = now
        db.commit()
        db.refresh(run)
        return run

    def _generate_live_recommendations(self, db: Session, user_id: UUID) -> list[RecommendationItem]:
        user_key = str(user_id)
        watched_rows = db.scalars(
            select(UserContentInteraction).where(UserContentInteraction.user_id == user_key)
        ).all()
        watched = {row.content_id for row in watched_rows}

        trending = db.scalars(select(TrendingContent).order_by(TrendingContent.score.desc()).limit(self.list_size * 2)).all()
        items: list[RecommendationItem] = []
        for row in trending:
            if row.content_id in watched:
                continue
            items.append(
                RecommendationItem(
                    content_id=UUID(row.content_id),
                    score=row.score,
                    reason="fallback_trending",
                )
            )
            if len(items) >= self.list_size:
                break
        return items

    def _fallback_content_ids(self, db: Session, watched: set[str]) -> list[str]:
        trending = db.scalars(select(TrendingContent).order_by(TrendingContent.score.desc()).limit(self.list_size * 2)).all()
        result: list[str] = []
        for row in trending:
            if row.content_id in watched:
                continue
            result.append(row.content_id)
            if len(result) >= self.list_size:
                break
        return result
