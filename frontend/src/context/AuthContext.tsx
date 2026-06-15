import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import type { AuthResponse, UserProfile } from "@/types";
import * as userApi from "@/api/user";
import { isJwtExpired } from "@/lib/jwt";

const STORAGE_KEY = "dls_auth";

interface StoredAuth {
  token: string;
  user: UserProfile;
}

interface AuthContextValue {
  token: string | null;
  user: UserProfile | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<string | null>;
  completeOAuthLogin: (token: string) => Promise<string | null>;
  register: (
    email: string,
    password: string,
    displayName: string,
  ) => Promise<string | null>;
  logout: () => void;
  refreshProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function loadStored(): StoredAuth | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    return JSON.parse(raw) as StoredAuth;
  } catch {
    return null;
  }
}

function sanitizeStored(stored: StoredAuth | null): StoredAuth | null {
  if (!stored?.token || isJwtExpired(stored.token)) return null;
  return stored;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const rawStored = loadStored();
  const stored = sanitizeStored(rawStored);
  const [token, setToken] = useState<string | null>(stored?.token ?? null);
  const [user, setUser] = useState<UserProfile | null>(stored?.user ?? null);

  const persist = useCallback((auth: StoredAuth | null) => {
    if (auth) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, []);

  useEffect(() => {
    if (rawStored && !stored) {
      persist(null);
    }
  }, [rawStored, stored, persist]);

  const applyAuth = useCallback(
    (auth: AuthResponse) => {
      setToken(auth.accessToken);
      setUser(auth.user);
      persist({ token: auth.accessToken, user: auth.user });
    },
    [persist],
  );

  const login = useCallback(
    async (email: string, password: string) => {
      const result = await userApi.login(email, password);
      if (!result.ok || !result.data) {
        return result.error ?? "Login failed";
      }
      applyAuth(result.data);
      return null;
    },
    [applyAuth],
  );

  const completeOAuthLogin = useCallback(
    async (token: string) => {
      const result = await userApi.completeOAuth(token);
      if (!result.ok || !result.data) {
        return result.error ?? "Google sign-in failed";
      }
      applyAuth(result.data);
      return null;
    },
    [applyAuth],
  );

  const register = useCallback(
    async (email: string, password: string, displayName: string) => {
      const result = await userApi.register(email, password, displayName);
      if (!result.ok || !result.data) {
        return result.error ?? "Registration failed";
      }
      applyAuth(result.data);
      return null;
    },
    [applyAuth],
  );

  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
    persist(null);
  }, [persist]);

  const refreshProfile = useCallback(async () => {
    if (!token) return;
    if (isJwtExpired(token)) {
      logout();
      return;
    }
    const result = await userApi.getMe(token);
    if (result.ok && result.data) {
      setUser(result.data);
      persist({ token, user: result.data });
    } else if (result.status === 401 || result.status === 403) {
      logout();
    }
  }, [token, persist, logout]);

  useEffect(() => {
    if (!token) return;
    if (isJwtExpired(token)) {
      logout();
      return;
    }
    void refreshProfile();
  }, [token, logout, refreshProfile]);

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token && user),
      login,
      completeOAuthLogin,
      register,
      logout,
      refreshProfile,
    }),
    [token, user, login, completeOAuthLogin, register, logout, refreshProfile],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}