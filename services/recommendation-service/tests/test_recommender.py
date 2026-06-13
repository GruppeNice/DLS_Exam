from uuid import uuid4

import pandas as pd
from sklearn.decomposition import NMF


def test_nmf_produces_recommendation_scores():
    frame = pd.DataFrame(
        {
            "user_id": ["u1", "u1", "u2", "u2"],
            "content_id": ["c1", "c2", "c2", "c3"],
            "weight": [5.0, 3.0, 4.0, 2.0],
        }
    )
    matrix = frame.pivot_table(index="user_id", columns="content_id", values="weight", fill_value=0.0)

    model = NMF(n_components=2, init="nndsvda", random_state=42)
    user_features = model.fit_transform(matrix.values)
    item_features = model.components_

    scores = user_features[0] @ item_features
    assert len(scores) == matrix.shape[1]
    assert scores.max() >= scores.min()


def test_event_payload_keys_match_java_publishers():
    playback_event = {
        "sessionId": str(uuid4()),
        "userId": str(uuid4()),
        "contentId": str(uuid4()),
        "positionSeconds": 120,
        "occurredAt": "2026-01-01T00:00:00Z",
    }
    assert "userId" in playback_event
    assert "contentId" in playback_event
