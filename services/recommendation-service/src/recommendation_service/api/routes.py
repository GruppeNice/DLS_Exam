from fastapi import APIRouter, Depends, Request
from sqlalchemy.orm import Session

from recommendation_service.schemas.recommendations import (
    HealthResponse,
    InteractionIngestRequest,
    RecommendationListResponse,
    RetrainResponse,
    TrendingListResponse,
)
from recommendation_service.security.jwt import UserPrincipal, get_current_user
from recommendation_service.services.interaction_service import InteractionService
from recommendation_service.services.recommender import RecommenderService
from recommendation_service.services.trending_service import TrendingService

router = APIRouter(prefix="/api/v1")


def get_db_session(request: Request):
    session_factory = request.app.state.session_factory
    db = session_factory()
    try:
        yield db
    finally:
        db.close()


@router.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse()


@router.get("/recommendations/me", response_model=RecommendationListResponse)
def my_recommendations(
    current_user: UserPrincipal = Depends(get_current_user),
    db: Session = Depends(get_db_session),
) -> RecommendationListResponse:
    from recommendation_service.main import recommender_service

    return recommender_service.get_recommendations_for_user(db, current_user.id)


@router.get("/recommendations/trending", response_model=TrendingListResponse)
def trending(
    request: Request,
    db: Session = Depends(get_db_session),
) -> TrendingListResponse:
    from recommendation_service.main import recommender_service, trending_service

    return trending_service.get_trending(db, limit=request.app.state.settings.recommendation_list_size)


@router.post("/recommendations/retrain", response_model=RetrainResponse)
def retrain(db: Session = Depends(get_db_session)) -> RetrainResponse:
    from recommendation_service.main import recommender_service

    run = recommender_service.retrain(db)
    return RetrainResponse(
        status=run.status,
        model_type=run.model_type,
        user_count=run.user_count,
        content_count=run.content_count,
        message=run.details or "Retrain completed",
    )


@router.post("/recommendations/interactions")
def ingest_interaction(
    request: InteractionIngestRequest,
    db: Session = Depends(get_db_session),
) -> dict[str, str]:
    interaction_service = InteractionService()
    interaction_service.upsert_interaction(
        db,
        user_id=request.user_id,
        content_id=request.content_id,
        weight_delta=request.weight,
        source_event=request.source_event,
    )
    return {"status": "accepted"}
