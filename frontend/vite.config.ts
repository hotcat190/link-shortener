import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import os from "os";

// Helper to get local IP address
function getLocalExternalIP() {
  const interfaces = os.networkInterfaces();
  for (const name of Object.keys(interfaces)) {
    for (const iface of interfaces[name]) {
      if (iface.family === "IPv4" && !iface.internal) {
        return iface.address;
      }
    }
  }
  return "localhost";
}

const localIP = getLocalExternalIP();

export default defineConfig({
  plugins: [react()],
  define: {
    __LOCAL_IP__: JSON.stringify(localIP),
  },
  server: {
    host: "0.0.0.0",
    port: 5173,
    proxy: {
      "/api": {
        target: `http://${localIP}`, // your backend
        changeOrigin: true,
      },
    },
  },
});
