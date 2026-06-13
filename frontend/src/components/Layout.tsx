import { NavLink, Outlet, Navigate } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";

const NAV = [
  { to: "/", label: "Overview", end: true },
  { to: "/catalog", label: "Catalog" },
  { to: "/playback", label: "Playback" },
  { to: "/billing", label: "Billing" },
  { to: "/reviews", label: "Reviews" },
  { to: "/notifications", label: "Notifications" },
  { to: "/recommendations", label: "Recommendations" },
];

export function AppLayout() {
  const { user, logout, isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">DLS</span>
          <div>
            <strong>Stream Console</strong>
            <p>7-service test UI</p>
          </div>
        </div>

        <nav className="nav">
          {NAV.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="user-chip">
            <span className="avatar">{user?.displayName?.[0] ?? "?"}</span>
            <div>
              <strong>{user?.displayName}</strong>
              <p>{user?.email}</p>
            </div>
          </div>
          <button type="button" className="btn btn-ghost" onClick={logout}>
            Sign out
          </button>
        </div>
      </aside>

      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}
