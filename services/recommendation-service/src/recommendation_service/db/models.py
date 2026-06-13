from datetime import datetime
from pathlib import Path

from sqlalchemy import DateTime, Float, Integer, String, Text, create_engine, func, inspect, JSON
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, sessionmaker


class Base(DeclarativeBase):
    pass


class UserContentInteraction(Base):
    __tablename__ = "user_content_interactions"

    id: Mapped[int] = mapped_column(primary_key=True)
    user_id: Mapped[str] = mapped_column(String(36), nullable=False)
    content_id: Mapped[str] = mapped_column(String(36), nullable=False)
    interaction_weight: Mapped[float] = mapped_column(Float, default=0.0)
    source_event: Mapped[str] = mapped_column(String(60), nullable=False)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())


class UserRecommendation(Base):
    __tablename__ = "user_recommendations"

    id: Mapped[int] = mapped_column(primary_key=True)
    user_id: Mapped[str] = mapped_column(String(36), unique=True, nullable=False)
    content_ids: Mapped[list] = mapped_column(JSON, nullable=False)
    generated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())


class TrendingContent(Base):
    __tablename__ = "trending_content"

    id: Mapped[int] = mapped_column(primary_key=True)
    content_id: Mapped[str] = mapped_column(String(36), unique=True, nullable=False)
    score: Mapped[float] = mapped_column(Float, default=0.0)
    play_count: Mapped[int] = mapped_column(Integer, default=0)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())


class ModelRun(Base):
    __tablename__ = "model_runs"

    id: Mapped[int] = mapped_column(primary_key=True)
    model_type: Mapped[str] = mapped_column(String(40), nullable=False)
    status: Mapped[str] = mapped_column(String(20), nullable=False)
    user_count: Mapped[int] = mapped_column(Integer, default=0)
    content_count: Mapped[int] = mapped_column(Integer, default=0)
    started_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
    completed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
    details: Mapped[str | None] = mapped_column(Text, nullable=True)


def create_session_factory(database_url: str):
    engine = create_engine(database_url, pool_pre_ping=True)
    return engine, sessionmaker(bind=engine, autoflush=False, autocommit=False)


def init_database(engine, *, apply_sql_schema: bool = True) -> None:
    if apply_sql_schema:
        schema_path = Path(__file__).with_name("schema.sql")
        inspector = inspect(engine)
        if schema_path.exists() and not inspector.has_table("user_content_interactions"):
            statements = [
                statement.strip()
                for statement in schema_path.read_text(encoding="utf-8").split(";")
                if statement.strip()
            ]
            with engine.begin() as connection:
                for statement in statements:
                    connection.exec_driver_sql(statement)
    Base.metadata.create_all(bind=engine)
    inspector = inspect(engine)
    if inspector.has_table("model_runs"):
        columns = {column["name"] for column in inspector.get_columns("model_runs")}
        if "details" not in columns:
            with engine.begin() as connection:
                connection.exec_driver_sql("ALTER TABLE model_runs ADD COLUMN details TEXT NULL")
