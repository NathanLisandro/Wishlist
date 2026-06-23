"use client";

import { useCallback, useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { toast } from "sonner";
import { Users, Heart } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { getAdminToken, getAdminNome, setAdmin } from "@/lib/auth";
import type { ConvitePreview, LoginResponse } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Brand } from "@/components/Brand";

export default function ConvitePage() {
  const router = useRouter();
  const { token } = useParams<{ token: string }>();

  const [preview, setPreview] = useState<ConvitePreview | null>(null);
  const [invalido, setInvalido] = useState(false);
  const [logado, setLogado] = useState<string | null>(null);
  // Quem recebe o convite normalmente ainda não tem conta -> abre em "Criar conta".
  const [modo, setModo] = useState<"entrar" | "criar">("criar");
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [carregando, setCarregando] = useState(false);

  useEffect(() => {
    setLogado(getAdminToken() ? getAdminNome() ?? "sua conta" : null);
    api
      .get<ConvitePreview>(`/manage-invites/${token}`)
      .then(setPreview)
      .catch(() => setInvalido(true));
  }, [token]);

  const aceitar = useCallback(
    async (jwt: string) => {
      const r = await api.post<{ eventoId: string }>(
        `/manage-invites/${token}/accept`,
        undefined,
        jwt,
      );
      toast.success("Pronto! Você agora co-gerencia este evento.");
      router.replace(`/admin/eventos/${r.eventoId}`);
    },
    [token, router],
  );

  async function aceitarLogado() {
    const jwt = getAdminToken();
    if (!jwt) return;
    setCarregando(true);
    try {
      await aceitar(jwt);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Não foi possível aceitar");
      setCarregando(false);
    }
  }

  async function entrarEAceitar(e: React.FormEvent) {
    e.preventDefault();
    setCarregando(true);
    try {
      if (modo === "criar") {
        await api.post("/auth/register", { email, senha, nomeExibicao: nome });
      }
      const login = await api.post<LoginResponse>("/auth/login", { email, senha });
      setAdmin(login.token, login.nomeExibicao);
      await aceitar(login.token);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Não foi possível continuar");
      setCarregando(false);
    }
  }

  if (invalido) {
    return (
      <main className="flex flex-1 items-center justify-center p-6 text-center">
        <p className="text-muted-foreground">Convite inválido ou expirado.</p>
      </main>
    );
  }

  return (
    <main className="flex flex-1 flex-col items-center justify-center gap-7 p-6">
      <Brand />
      <Card className="w-full max-w-sm rounded-[22px] border-border shadow-[0_30px_60px_-34px_rgba(110,31,67,0.4)]">
        <CardContent className="space-y-5 p-7">
          <div className="text-center">
            <span className="brand-gradient mx-auto grid size-12 place-items-center rounded-2xl text-white">
              <Users className="size-6" />
            </span>
            <h1 className="mt-3 font-serif text-2xl">Convite de co-gestão</h1>
            <p className="mt-1 text-sm text-muted-foreground">
              Você foi convidado(a) para ajudar a gerenciar{" "}
              <strong className="text-foreground">{preview?.nomeEvento ?? "…"}</strong>.
            </p>
          </div>

          {logado ? (
            <div className="space-y-3">
              <p className="text-center text-sm text-muted-foreground">
                Você está logado como <strong>{logado}</strong>.
              </p>
              <Button
                className="brand-gradient h-11 w-full border-0 text-white hover:opacity-95"
                onClick={aceitarLogado}
                disabled={carregando}
              >
                <Heart className="size-4" /> {carregando ? "Aceitando..." : "Aceitar e co-gerenciar"}
              </Button>
              <button
                type="button"
                onClick={() => {
                  setLogado(null);
                }}
                className="mx-auto block w-fit cursor-pointer text-sm text-muted-foreground hover:text-plum"
              >
                Entrar com outra conta
              </button>
            </div>
          ) : (
            <>
              <div className="flex rounded-lg border border-border bg-muted/40 p-1">
                {(["criar", "entrar"] as const).map((m) => (
                  <button
                    key={m}
                    type="button"
                    onClick={() => setModo(m)}
                    className={
                      "flex-1 cursor-pointer rounded-md py-1.5 text-sm font-medium transition-colors " +
                      (modo === m ? "bg-white text-plum shadow-sm" : "text-muted-foreground")
                    }
                  >
                    {m === "entrar" ? "Já tenho conta" : "Criar conta"}
                  </button>
                ))}
              </div>

              <form onSubmit={entrarEAceitar} className="space-y-4">
                {modo === "criar" && (
                  <div className="space-y-2">
                    <Label htmlFor="nome">Seu nome</Label>
                    <Input
                      id="nome"
                      className="h-11"
                      value={nome}
                      onChange={(e) => setNome(e.target.value)}
                      required
                    />
                  </div>
                )}
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
                  <Label htmlFor="senha">Senha {modo === "criar" && "(mín. 8 caracteres)"}</Label>
                  <Input
                    id="senha"
                    type="password"
                    minLength={modo === "criar" ? 8 : undefined}
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
                  {carregando
                    ? "Aguarde..."
                    : modo === "criar"
                      ? "Criar conta e aceitar"
                      : "Entrar e aceitar"}
                </Button>
              </form>
            </>
          )}
        </CardContent>
      </Card>
    </main>
  );
}
