import logging
import os
from contextlib import asynccontextmanager

from apscheduler.schedulers.background import BackgroundScheduler
from fastapi import FastAPI
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from recommendation_service.api.routes import router
from recommendation_service.config import Settings, get_settings
from recommendation_service.db.models import create_session_factory, init_database
from recommendation_service.messaging.consumer import RabbitMqConsumer
from recommendation_service.services.recommender import RecommenderService
from recommendation_service.services.trending_service import TrendingService

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

settings = get_settings()
recommender_service = RecommenderService(list_size=settings.recommendation_list_size)
trending_service = TrendingService()


def _build_session_factory(app_settings: Settings, testing: bool = False):
    if testing:
        engine = create_engine(
            "sqlite+pysqlite:///:memory:",
            connect_args={"check_same_thread": False},
            poolclass=StaticPool,
        )
        session_factory = sessionmaker(bind=engine, autoflush=False, autocommit=False)
        init_database(engine, apply_sql_schema=False)
        return engine, session_factory

    engine, session_factory = create_session_factory(app_settings.database_url)
    init_database(engine, apply_sql_schema=True)
    return engine, session_factory


def create_app(app_settings: Settings | None = None, testing: bool = False) -> FastAPI:
    app_settings = app_settings or get_settings()
    engine, session_factory = _build_session_factory(app_settings, testing=testing)
    rabbit_consumer = RabbitMqConsumer(settings=app_settings, session_factory=session_factory)
    scheduler = BackgroundScheduler()

    def _scheduled_retrain() -> None:
        db = session_factory()
        try:
            run = recommender_service.retrain(db)
            logger.info("Scheduled retrain finished with status=%s", run.status)
        except Exception:
            logger.exception("Scheduled retrain failed")
        finally:
            db.close()

    @asynccontextmanager
    async def lifespan(_: FastAPI):
        if not testing and os.getenv("DISABLE_BACKGROUND_JOBS") != "1":
            rabbit_consumer.start()
            scheduler.add_job(
                _scheduled_retrain,
                trigger="interval",
                minutes=app_settings.model_retrain_interval_minutes,
                id="model-retrain",
                replace_existing=True,
            )
            scheduler.start()
        logger.info("Recommendation service started")
        yield
        if not testing and os.getenv("DISABLE_BACKGROUND_JOBS") != "1":
            scheduler.shutdown(wait=False)
            rabbit_consumer.stop()

    app = FastAPI(
        title="Recommendation Service API",
        version="1.0.0",
        description="AI recommendation microservice for DLS Exam",
        lifespan=lifespan,
    )
    app.state.settings = app_settings
    app.state.session_factory = session_factory
    app.include_router(router)
    return app


app = create_app(testing=os.getenv("TESTING") == "1")
