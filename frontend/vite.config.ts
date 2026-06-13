import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "node:path";

const proxy = (target: string, prefix: string) => ({
  target,
  changeOrigin: true,
  rewrite: (p: string) => p.replace(new RegExp(`^${prefix}`), ""),
});

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
    },
  },
  server: {
    port: 3000,
    proxy: {
      "/api/user": proxy("http://localhost:8081", "/api/user"),
      "/api/catalog": proxy("http://localhost:8082", "/api/catalog"),
      "/api/streaming": proxy("http://localhost:8083", "/api/streaming"),
      "/api/billing": proxy("http://localhost:8084", "/api/billing"),
      "/api/review": proxy("http://localhost:8085", "/api/review"),
      "/api/engagement": proxy("http://localhost:8086", "/api/engagement"),
      "/api/recommendation": proxy("http://localhost:8090", "/api/recommendation"),
    },
  },
});
