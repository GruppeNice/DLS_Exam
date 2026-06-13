from datetime import datetime, timezone
from uuid import UUID

from sqlalchemy import select
from sqlalchemy.orm import Session

from recommendation_service.db.models import TrendingContent
from recommendation_service.schemas.recommendations import RecommendationItem, TrendingListResponse


class TrendingService:
    def get_trending(self, db: Session, limit: int = 10) -> TrendingListResponse:
        rows = db.scalars(
            select(TrendingContent).order_by(TrendingContent.score.desc()).limit(limit)
        ).all()

        items = [
            RecommendationItem(
                content_id=UUID(row.content_id),
                score=row.score,
                reason="trending_aggregate",
            )
            for row in rows
        ]

        return TrendingListResponse(items=items, generated_at=datetime.now(timezone.utc))
