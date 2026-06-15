import { useEffect, useState } from "react";
import { Navigate, useSearchParams } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";

export function OAuthCallbackPage() {
  const [searchParams] = useSearchParams();
  const { isAuthenticated, completeOAuthLogin } = useAuth();
  const [error, setError] = useState<string | null>(null);
  const [done, setDone] = useState(false);

  useEffect(() => {
    const token = searchParams.get("token");
    if (!token) {
      setError("Missing token from OAuth callback");
      return;
    }

    void (async () => {
      const err = await completeOAuthLogin(token);
      if (err) {
        setError(err);
      } else {
        setDone(true);
      }
    })();
  }, [searchParams, completeOAuthLogin]);

  if (isAuthenticated && done) {
    return <Navigate to="/" replace />;
  }

  if (error) {
    return (
      <div className="login-page">
        <div className="login-card">
          <h1>Google sign-in failed</h1>
          <p className="form-error">{error}</p>
          <a href="/login">Back to sign in</a>
        </div>
      </div>
    );
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>Completing Google sign-in…</h1>
      </div>
    </div>
  );
}
