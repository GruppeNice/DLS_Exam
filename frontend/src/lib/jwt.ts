export function isJwtExpired(token: string, skewSeconds = 30): boolean {
  try {
    const payload = token.split(".")[1];
    if (!payload) return true;
    const decoded = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/"))) as {
      exp?: number;
    };
    if (!decoded.exp) return false;
    return Date.now() / 1000 >= decoded.exp - skewSeconds;
  } catch {
    return true;
  }
}
