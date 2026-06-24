"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { toast } from "sonner";
import { api, ApiError } from "@/lib/api";
import { setAdmin } from "@/lib/auth";
import { resetarOnboardingLocal } from "@/lib/onboarding";
import type { LoginResponse } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Brand } from "@/components/Brand";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [carregando, setCarregando] = useState(false);

  async function entrar(e: React.FormEvent) {
    e.preventDefault();
    setCarregando(true);
    try {
      const r = await api.post<LoginResponse>("/auth/login", { email, senha });
      setAdmin(r.token, r.nomeExibicao);
      resetarOnboardingLocal(); // cache é por navegador; nova sessão reavalia via /me
      router.push("/admin/eventos");
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Falha no login");
      setCarregando(false);
    }
  }

  return (
    <main className="flex flex-1 flex-col items-center justify-center gap-7 p-6">
      <Brand />
      <Card className="w-full max-w-sm rounded-[22px] border-border shadow-[0_30px_60px_-34px_rgba(110,31,67,0.4)]">
        <CardContent className="space-y-5 p-7">
          <div>
            <h1 className="font-serif text-3xl">Área do casal</h1>
            <p className="mt-1 text-sm text-muted-foreground">
              Entre para gerenciar seu evento.
            </p>
          </div>
          <form onSubmit={entrar} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">E-mail</Label>
              <Input
                id="email"
                type="email"
                className="h-11"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="senha">Senha</Label>
              <Input
                id="senha"
                type="password"
                className="h-11"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                required
              />
            </div>
            <Button
              type="submit"
              className="brand-gradient h-11 w-full border-0 text-white hover:opacity-95"
              disabled={carregando}
            >
              {carregando ? "Entrando..." : "Entrar"}
            </Button>
            <p className="text-center text-sm text-muted-foreground">
              Não tem conta?{" "}
              <Link
                href="/admin/register"
                className="font-medium text-plum underline-offset-4 hover:underline"
              >
                Criar conta
              </Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </main>
  );
}
