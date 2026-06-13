import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "@/context/AuthContext";
import { AppLayout } from "@/components/Layout";
import { LoginPage } from "@/pages/LoginPage";
import { OverviewPage } from "@/pages/OverviewPage";
import { CatalogPage } from "@/pages/CatalogPage";
import { PlaybackPage } from "@/pages/PlaybackPage";
import { BillingPage } from "@/pages/BillingPage";
import { ReviewsPage } from "@/pages/ReviewsPage";
import { NotificationsPage } from "@/pages/NotificationsPage";
import { RecommendationsPage } from "@/pages/RecommendationsPage";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route element={<AppLayout />}>
            <Route path="/" element={<OverviewPage />} />
            <Route path="/catalog" element={<CatalogPage />} />
            <Route path="/playback" element={<PlaybackPage />} />
            <Route path="/billing" element={<BillingPage />} />
            <Route path="/reviews" element={<ReviewsPage />} />
            <Route path="/notifications" element={<NotificationsPage />} />
            <Route path="/recommendations" element={<RecommendationsPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
