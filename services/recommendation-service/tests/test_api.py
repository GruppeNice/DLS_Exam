
def test_health_endpoint(client):
    response = client.get("/api/v1/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"


def test_trending_endpoint(client):
    response = client.get("/api/v1/recommendations/trending")
    assert response.status_code == 200
    assert "items" in response.json()
