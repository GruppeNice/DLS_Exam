from datetime import datetime, timezone
from uuid import UUID

from sqlalchemy import select
from sqlalchemy.orm import Session

from recommendation_service.db.models import TrendingContent, UserContentInteraction


class InteractionService:
    def upsert_interaction(
        self,
        db: Session,
        user_id: UUID,
        content_id: UUID,
        weight_delta: float,
        source_event: str,
    ) -> None:
        user_key = str(user_id)
        content_key = str(content_id)

        interaction = db.scalar(
            select(UserContentInteraction).where(
                UserContentInteraction.user_id == user_key,
                UserContentInteraction.content_id == content_key,
            )
        )

        if interaction is None:
            interaction = UserContentInteraction(
                user_id=user_key,
                content_id=content_key,
                interaction_weight=max(weight_delta, 0.0),
                source_event=source_event,
                updated_at=datetime.now(timezone.utc),
            )
            db.add(interaction)
        else:
            interaction.interaction_weight = max(interaction.interaction_weight + weight_delta, 0.0)
            interaction.source_event = source_event
            interaction.updated_at = datetime.now(timezone.utc)

        self._update_trending(db, content_key, weight_delta)
        db.commit()

    def _update_trending(self, db: Session, content_id: str, weight_delta: float) -> None:
        trending = db.scalar(select(TrendingContent).where(TrendingContent.content_id == content_id))
        if trending is None:
            trending = TrendingContent(
                content_id=content_id,
                score=max(weight_delta, 0.0),
                play_count=1 if weight_delta > 0 else 0,
                updated_at=datetime.now(timezone.utc),
            )
            db.add(trending)
        else:
            trending.score = max(trending.score + weight_delta, 0.0)
            if weight_delta > 0:
                trending.play_count += 1
            trending.updated_at = datetime.now(timezone.utc)
