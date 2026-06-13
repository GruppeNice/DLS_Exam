from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore", protected_namespaces=("settings_",))

    server_port: int = 8090
    database_url: str = (
        "postgresql+psycopg2://recommendation_user:recommendation_password@localhost:5435/recommendation_db"
    )
    rabbitmq_host: str = "localhost"
    rabbitmq_port: int = 5675
    rabbitmq_username: str = "guest"
    rabbitmq_password: str = "guest"
    streaming_events_exchange: str = "streaming.events"
    billing_events_exchange: str = "billing.events"
    review_events_exchange: str = "review.events"
    jwt_secret_base64: str = (
        "VGVzdGluZ0Rldk9ubHlTZWNyZXRLZXlGb3JKV1QxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMw=="
    )
    model_retrain_interval_minutes: int = 30
    recommendation_list_size: int = 10


@lru_cache
def get_settings() -> Settings:
    return Settings()
