from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, Field


class RecommendationItem(BaseModel):
    content_id: UUID
    score: float
    reason: str


class RecommendationListResponse(BaseModel):
    user_id: UUID
    items: list[RecommendationItem]
    generated_at: datetime
    model_type: str


class TrendingListResponse(BaseModel):
    items: list[RecommendationItem]
    generated_at: datetime


class RetrainResponse(BaseModel):
    status: str
    model_type: str
    user_count: int
    content_count: int
    message: str


class HealthResponse(BaseModel):
    status: str = "ok"
    service: str = "recommendation-service"


class InteractionIngestRequest(BaseModel):
    user_id: UUID
    content_id: UUID
    weight: float = Field(ge=0)
    source_event: str
