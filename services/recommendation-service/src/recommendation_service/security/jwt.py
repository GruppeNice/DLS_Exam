import base64
from dataclasses import dataclass
from typing import Annotated
from uuid import UUID

import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from recommendation_service.config import get_settings

security = HTTPBearer(auto_error=False)


@dataclass(frozen=True)
class UserPrincipal:
    id: UUID
    email: str
    roles: set[str]


def _decode_token(token: str) -> UserPrincipal:
    settings = get_settings()
    secret = base64.b64decode(settings.jwt_secret_base64)
    try:
        claims = jwt.decode(token, secret, algorithms=["HS256"])
    except jwt.PyJWTError as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token") from exc

    email = claims.get("sub")
    user_id = claims.get("uid")
    if not email or not user_id:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token claims")

    roles = claims.get("roles", ["USER"])
    if isinstance(roles, list):
        role_set = {str(role) for role in roles}
    else:
        role_set = {"USER"}

    return UserPrincipal(id=UUID(str(user_id)), email=str(email), roles=role_set)


def get_current_user(
    credentials: Annotated[HTTPAuthorizationCredentials | None, Depends(security)],
) -> UserPrincipal:
    if credentials is None or credentials.scheme.lower() != "bearer":
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing bearer token")
    return _decode_token(credentials.credentials)
