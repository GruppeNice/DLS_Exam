import { useState } from "react";
import { useAuth } from "@/context/AuthContext";
import * as engagementApi from "@/api/engagement";
import type { ApiResult } from "@/types";
import { ActionButton, Field, PageHeader, ResponsePanel } from "@/components/ui";

export function NotificationsPage() {
  const { user } = useAuth();
  const [recipient, setRecipient] = useState(user?.email ?? "demo@dls.local");
  const [subject, setSubject] = useState("Welcome to DLS Stream");
  const [template, setTemplate] = useState("welcome");
  const [notificationId, setNotificationId] = useState(1);
  const [sendResult, setSendResult] = useState<ApiResult<unknown> | null>(null);
  const [lookup, setLookup] = useState<ApiResult<unknown> | null>(null);
  const [loading, setLoading] = useState(false);

  async function send() {
    setLoading(true);
    const result = await engagementApi.sendNotification({
      type: "EMAIL",
      recipient,
      subject,
      templateName: template,
      templateVariables: { userName: user?.displayName ?? "Viewer", activationLink: "http://localhost:3000" },
    });
    setSendResult(result);
    if (result.ok && result.data && typeof result.data === "object" && "notificationId" in result.data) {
      setNotificationId(Number((result.data as { notificationId: number }).notificationId));
    }
    setLoading(false);
  }

  async function fetchStatus() {
    setLoading(true);
    const result = await engagementApi.getNotification(notificationId);
    setLookup(result);
    setLoading(false);
  }

  return (
    <div className="page">
      <PageHeader
        title="Notifications"
        description="Queue email notifications via engagement-service. Emails are captured by MailHog when running Docker Compose."
        service="engagement-service"
        port={8086}
      />

      <div className="two-col">
        <section className="panel">
          <Field label="Recipient">
            <input value={recipient} onChange={(e) => setRecipient(e.target.value)} />
          </Field>
          <Field label="Subject">
            <input value={subject} onChange={(e) => setSubject(e.target.value)} />
          </Field>
          <Field label="Template name">
            <input value={template} onChange={(e) => setTemplate(e.target.value)} />
          </Field>
          <ActionButton label="Send notification" onClick={send} disabled={loading} />
          <ResponsePanel title="Send result" result={sendResult} loading={loading} />
        </section>

        <section className="panel">
          <Field label="Notification ID">
            <input
              type="number"
              value={notificationId}
              onChange={(e) => setNotificationId(Number(e.target.value))}
            />
          </Field>
          <ActionButton label="Check status" onClick={fetchStatus} variant="secondary" disabled={loading} />
          <ResponsePanel title="Notification status" result={lookup} loading={loading} />
          <p className="muted">
            Open <a href="http://localhost:8025" target="_blank" rel="noreferrer">MailHog</a> to view delivered emails.
          </p>
        </section>
      </div>
    </div>
  );
}
