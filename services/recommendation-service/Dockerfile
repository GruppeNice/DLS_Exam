FROM python:3.12-slim

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONPATH=/app/src

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY src ./src
COPY api ./api

EXPOSE 8090

CMD ["uvicorn", "recommendation_service.main:app", "--host", "0.0.0.0", "--port", "8090", "--app-dir", "/app/src"]
