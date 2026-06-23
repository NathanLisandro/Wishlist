import type { NextConfig } from "next";

// Backend interno (mesma máquina). O navegador NUNCA fala direto com ele: usa
// caminhos relativos e o Next faz proxy daqui para o backend. Isso permite expor
// só o frontend por UM túnel ngrok (sem CORS no browser e sem a página de aviso
// do ngrok quebrar API/imagens).
const BACKEND = process.env.API_INTERNAL_URL ?? "http://localhost:8080";

// Prefixos servidos pelo backend Spring (e o /media das fotos).
const PREFIXOS = [
  "auth", "account", "events", "public", "webhooks", "actuator", "media", "manage-invites",
];

const nextConfig: NextConfig = {
  // Libera o host do túnel de teste no celular para o dev server do Next.
  allowedDevOrigins: ["*.ngrok-free.app", "*.ngrok.app", "*.ngrok.io", "*.ngrok-free.dev"],

  async rewrites() {
    const exato = PREFIXOS.map((p) => ({ source: `/${p}`, destination: `${BACKEND}/${p}` }));
    const curinga = PREFIXOS.map((p) => ({
      source: `/${p}/:path*`,
      destination: `${BACKEND}/${p}/:path*`,
    }));
    return [...exato, ...curinga];
  },
};

export default nextConfig;
