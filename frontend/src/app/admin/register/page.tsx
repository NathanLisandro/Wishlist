"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { toast } from "sonner";
import { api, ApiError } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Brand } from "@/components/Brand";

export default function RegisterPage() {
  const router = useRouter();
  const [nomeExibicao, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [carregando, setCarregando] = useState(false);

  async function registrar(e: React.FormEvent) {
    e.preventDefault();
    setCarregando(true);
    try {
      await api.post("/auth/register", { email, senha, nomeExibicao });
      toast.success("Conta criada! Faça login.");
      router.push("/admin/login");
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Falha ao criar conta");
      setCarregando(false);
    }
  }

  return (
    <main className="flex flex-1 flex-col items-center justify-center gap-7 p-6">
      <Brand />
      <Card className="w-full max-w-sm rounded-[22px] border-border shadow-[0_30px_60px_-34px_rgba(110,31,67,0.4)]">
        <CardContent className="space-y-5 p-7">
          <div>
            <h1 className="font-serif text-3xl">Criar conta</h1>
            <p className="mt-1 text-sm text-muted-foreground">
              Comece a montar a lista do seu evento.
            </p>
          </div>
          <form onSubmit={registrar} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="nome">Nome do casal</Label>
              <Input
                id="nome"
                className="h-11"
                value={nomeExibicao}
                placeholder="Nathan & Thays"
                onChange={(e) => setNome(e.target.value)}
                required
              />
            </div>
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
              <Label htmlFor="senha">Senha (mín. 8 caracteres)</Label>
              <Input
                id="senha"
                type="password"
                minLength={8}
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
              {carregando ? "Criando..." : "Criar conta"}
            </Button>
            <p className="text-center text-sm text-muted-foreground">
              Já tem conta?{" "}
              <Link
                href="/admin/login"
                className="font-medium text-plum underline-offset-4 hover:underline"
              >
                Entrar
              </Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </main>
  );
}
