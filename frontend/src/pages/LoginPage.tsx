import { FormEvent, useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import * as userApi from "@/api/user";

const USER_SERVICE_URL = import.meta.env.VITE_USER_SERVICE_URL ?? "http://localhost:8081";

export function LoginPage() {
  const { login, register, isAuthenticated } = useAuth();
  const [mode, setMode] = useState<"login" | "register">("login");
  const [googleOAuthEnabled, setGoogleOAuthEnabled] = useState(false);
  const [email, setEmail] = useState("demo@dls.local");
  const [password, setPassword] = useState("password123");
  const [displayName, setDisplayName] = useState("Demo User");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    void userApi.getGoogleOAuthStatus().then((result) => {
      if (result.ok && result.data?.enabled) {
        setGoogleOAuthEnabled(true);
      }
    });
  }, []);

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    const err =
      mode === "login"
        ? await login(email, password)
        : await register(email, password, displayName);
    if (err) setError(err);
    setLoading(false);
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-hero">
          <p className="eyebrow">DLS Exam Platform</p>
          <h1>Stream Console</h1>
          <p>
            Sign in to exercise all seven microservices — user, catalog, streaming,
            billing, review, engagement, and recommendation — from one UI.
          </p>
        </div>

        <form className="login-form" onSubmit={handleSubmit}>
          <div className="tab-row">
            <button
              type="button"
              className={mode === "login" ? "tab active" : "tab"}
              onClick={() => setMode("login")}
            >
              Sign in
            </button>
            <button
              type="button"
              className={mode === "register" ? "tab active" : "tab"}
              onClick={() => setMode("register")}
            >
              Register
            </button>
          </div>

          {mode === "register" && (
            <label className="field">
              <span className="field-label">Display name</span>
              <input
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                required
              />
            </label>
          )}

          <label className="field">
            <span className="field-label">Email</span>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </label>

          <label className="field">
            <span className="field-label">Password</span>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={8}
            />
          </label>

          {error && <p className="form-error">{error}</p>}

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? "Please wait…" : mode === "login" ? "Sign in" : "Create account"}
          </button>

          {googleOAuthEnabled && mode === "login" && (
            <>
              <p className="login-divider">or</p>
              <a
                className="btn btn-secondary btn-block"
                href={userApi.googleOAuthAuthorizationUrl(USER_SERVICE_URL)}
              >
                Continue with Google
              </a>
            </>
          )}
        </form>
      </div>
    </div>
  );
}
