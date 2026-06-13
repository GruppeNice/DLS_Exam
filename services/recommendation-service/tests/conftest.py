import os

os.environ["TESTING"] = "1"

import pytest
from fastapi.testclient import TestClient

from recommendation_service.config import Settings
from recommendation_service.main import create_app


@pytest.fixture
def client():
    settings = Settings(
        database_url="sqlite+pysqlite:///:memory:",
        model_retrain_interval_minutes=60,
    )
    app = create_app(app_settings=settings, testing=True)
    with TestClient(app) as test_client:
        yield test_client
