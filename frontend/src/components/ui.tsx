import type { ReactNode } from "react";
import type { ApiResult } from "@/types";

interface ActionButtonProps {
  label: string;
  onClick: () => void | Promise<void>;
  disabled?: boolean;
  variant?: "primary" | "secondary" | "danger";
}

export function ActionButton({
  label,
  onClick,
  disabled,
  variant = "primary",
}: ActionButtonProps) {
  return (
    <button
      type="button"
      className={`btn btn-${variant}`}
      onClick={() => void onClick()}
      disabled={disabled}
    >
      {label}
    </button>
  );
}

interface FieldProps {
  label: string;
  children: ReactNode;
}

export function Field({ label, children }: FieldProps) {
  return (
    <label className="field">
      <span className="field-label">{label}</span>
      {children}
    </label>
  );
}

interface ResponsePanelProps<T> {
  title?: string;
  result: ApiResult<T> | null;
  loading?: boolean;
}

export function ResponsePanel<T>({ title = "Response", result, loading }: ResponsePanelProps<T>) {
  return (
    <div className="response-panel">
      <div className="response-header">
        <h3>{title}</h3>
        {loading && <span className="badge badge-muted">Loading…</span>}
        {result && (
          <span className={`badge ${result.ok ? "badge-ok" : "badge-error"}`}>
            {result.ok ? `${result.status} OK` : result.error ?? `Error ${result.status}`}
          </span>
        )}
      </div>
      <pre className="response-body">
        {loading
          ? "Waiting for response…"
          : result
            ? JSON.stringify(result.data ?? { error: result.error }, null, 2)
            : "Run an action to see the API response."}
      </pre>
    </div>
  );
}

interface StreamPageHeaderProps {
  title: string;
  description: string;
}

export function StreamPageHeader({ title, description }: StreamPageHeaderProps) {
  return (
    <header className="page-header stream-header">
      <div>
        <p className="eyebrow">DLS Stream</p>
        <h1>{title}</h1>
        <p className="page-description">{description}</p>
      </div>
    </header>
  );
}

interface StatusMessageProps {
  variant: "success" | "error" | "warning";
  children: ReactNode;
}

export function StatusMessage({ variant, children }: StatusMessageProps) {
  return <div className={`status-message status-${variant}`}>{children}</div>;
}

interface PageHeaderProps {
  title: string;
  description: string;
  service: string;
  port: number;
}

export function PageHeader({ title, description, service, port }: PageHeaderProps) {
  return (
    <header className="page-header">
      <div>
        <p className="eyebrow">{service} · :{port}</p>
        <h1>{title}</h1>
        <p className="page-description">{description}</p>
      </div>
    </header>
  );
}

interface StatusDotProps {
  up: boolean;
  label: string;
}

export function StatusDot({ up, label }: StatusDotProps) {
  return (
    <div className={`status-dot ${up ? "up" : "down"}`}>
      <span className="dot" />
      <span>{label}</span>
    </div>
  );
}
