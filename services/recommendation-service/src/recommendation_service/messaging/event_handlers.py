import json
import logging
from typing import Any
from uuid import UUID

from sqlalchemy.orm import Session

from recommendation_service.services.interaction_service import InteractionService

logger = logging.getLogger(__name__)


class EventHandler:
    def __init__(self, interaction_service: InteractionService | None = None):
        self.interaction_service = interaction_service or InteractionService()

    def handle(self, db: Session, routing_key: str, payload: dict[str, Any]) -> None:
        if routing_key == "playback.started":
            self._handle_playback_started(db, payload)
        elif routing_key == "playback.progress.updated":
            self._handle_playback_progress(db, payload)
        elif routing_key == "playback.stopped":
            self._handle_playback_stopped(db, payload)
        elif routing_key == "subscription.activated":
            self._handle_subscription_activated(db, payload)
        elif routing_key == "content.rated":
            self._handle_content_rated(db, payload)
        else:
            logger.debug("Ignoring unsupported routing key: %s", routing_key)

    def _handle_playback_started(self, db: Session, payload: dict[str, Any]) -> None:
        self.interaction_service.upsert_interaction(
            db,
            user_id=UUID(str(payload["userId"])),
            content_id=UUID(str(payload["contentId"])),
            weight_delta=1.0,
            source_event="playback.started",
        )

    def _handle_playback_progress(self, db: Session, payload: dict[str, Any]) -> None:
        position = float(payload.get("positionSeconds", 0))
        weight_delta = min(position / 300.0, 2.0)
        self.interaction_service.upsert_interaction(
            db,
            user_id=UUID(str(payload["userId"])),
            content_id=UUID(str(payload["contentId"])),
            weight_delta=weight_delta,
            source_event="playback.progress.updated",
        )

    def _handle_playback_stopped(self, db: Session, payload: dict[str, Any]) -> None:
        position = float(payload.get("positionSeconds", 0))
        weight_delta = min(position / 600.0, 1.5)
        self.interaction_service.upsert_interaction(
            db,
            user_id=UUID(str(payload["userId"])),
            content_id=UUID(str(payload["contentId"])),
            weight_delta=weight_delta,
            source_event="playback.stopped",
        )

    def _handle_subscription_activated(self, db: Session, payload: dict[str, Any]) -> None:
        logger.info("Subscription activated for user %s", payload.get("userId"))

    def _handle_content_rated(self, db: Session, payload: dict[str, Any]) -> None:
        stars = float(payload.get("stars", payload.get("rating", 3)))
        self.interaction_service.upsert_interaction(
            db,
            user_id=UUID(str(payload["userId"])),
            content_id=UUID(str(payload["contentId"])),
            weight_delta=stars,
            source_event="content.rated",
        )


def parse_message(body: bytes) -> dict[str, Any]:
    return json.loads(body.decode("utf-8"))
