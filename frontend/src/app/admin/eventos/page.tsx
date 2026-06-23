"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { toast } from "sonner";
import { CalendarDays, ExternalLink, ImageIcon, LogOut, Plus, Settings } from "lucide-react";
import { api, ApiError, mediaUrl } from "@/lib/api";
import { getAdminToken, getAdminNome, clearAdmin } from "@/lib/auth";
import type { EventoResumo } from "@/lib/types";
import { Button, buttonVariants } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Brand } from "@/components/Brand";

const CATEGORIAS = [
  { id: 1, label: "Casamento" },
  { id: 2, label: "Noivado" },
  { id: 3, label: "Chá de Panela" },
  { id: 4, label: "Chá de Cozinha" },
  { id: 5, label: "Chá Bar" },
  { id: 6, label: "Chá de Casa Nova" },
  { id: 7, label: "Chá de Lingerie" },
  { id: 8, label: "Bodas" },
  { id: 9, label: "Outro" },
];

export default function EventosPage() {
  const router = useRouter();
  const [token, setToken] = useState<string | null>(null);
  const [nomeAdmin, setNomeAdmin] = useState<string | null>(null);
  const [eventos, setEventos] = useState<EventoResumo[]>([]);
  const [nome, setNome] = useState("");
  const [slug, setSlug] = useState("");
  const [categoriaId, setCategoriaId] = useState(1);
  const [salvando, setSalvando] = useState(false);

  const carregar = useCallback(
    async (t: string) => {
      try {
        setEventos(await api.get<EventoResumo[]>("/events", t));
      } catch (err) {
        if (err instanceof ApiError && err.status === 401) {
          clearAdmin();
          router.replace("/admin/login");
        }
      }
    },
    [router],
  );

  useEffect(() => {
    const t = getAdminToken();
    if (!t) {
      router.replace("/admin/login");
      return;
    }
    setToken(t);
    setNomeAdmin(getAdminNome());
    carregar(t);
  }, [carregar, router]);

  function slugify(v: string) {
    return v
      .toLowerCase()
      .normalize("NFD")
      .replace(/[̀-ͯ]/g, "")
      .replace(/[^a-z0-9]+/g, "-")
      .replace(/^-+|-+$/g, "");
  }

  async function criar(e: React.FormEvent) {
    e.preventDefault();
    if (!token) return;
    setSalvando(true);
    try {
      await api.post(
        "/events",
        { categoriaId, slug, nome, rsvpHabilitado: true, presenteDinheiroHabilitado: true },
        token,
      );
      toast.success("Evento criado!");
      setNome("");
      setSlug("");
      await carregar(token);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro ao criar evento");
    } finally {
      setSalvando(false);
    }
  }

  function sair() {
    clearAdmin();
    router.replace("/admin/login");
  }

  return (
    <main className="mx-auto w-full max-w-4xl px-5 py-6 sm:px-6">
      <div className="flex items-center justify-between">
        <Brand href="/admin/eventos" sub={nomeAdmin ?? undefined} />
        <button
          onClick={sair}
          className="flex cursor-pointer items-center gap-1.5 rounded-lg border border-border px-3 py-1.5 text-[13px] font-medium text-muted-foreground transition-colors hover:border-lilac-line hover:bg-white hover:text-plum"
        >
          <LogOut className="size-3.5" /> Sair
        </button>
      </div>

      <h1 className="mt-8 font-serif text-4xl">Meus eventos</h1>

      <Card className="mt-5 rounded-2xl border-border">
        <CardContent className="p-5 sm:p-6">
          <div className="mb-4 flex items-center gap-2 text-sm font-medium">
            <span className="grid size-8 place-items-center rounded-lg bg-lilac text-plum">
              <Plus className="size-4" />
            </span>
            Novo evento
          </div>
          <form onSubmit={criar} className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="nome">Nome</Label>
              <Input
                id="nome"
                className="h-11"
                value={nome}
                placeholder="Nathan & Thays"
                onChange={(e) => {
                  setNome(e.target.value);
                  if (!slug) setSlug(slugify(e.target.value));
                }}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="slug">Link público (slug)</Label>
              <Input
                id="slug"
                className="h-11"
                value={slug}
                placeholder="nathan-e-thays"
                onChange={(e) => setSlug(slugify(e.target.value))}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="cat">Categoria</Label>
              <select
                id="cat"
                value={categoriaId}
                onChange={(e) => setCategoriaId(Number(e.target.value))}
                className="h-11 w-full cursor-pointer rounded-lg border border-input bg-white px-3 text-sm outline-none transition-colors focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/30"
              >
                {CATEGORIAS.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex items-end">
              <Button
                type="submit"
                className="brand-gradient h-11 w-full border-0 text-white hover:opacity-95"
                disabled={salvando}
              >
                {salvando ? "Salvando..." : "Criar evento"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <div className="mt-5 space-y-3">
        {eventos.length === 0 && (
          <p className="rounded-2xl border border-dashed border-border bg-white/50 p-8 text-center text-sm text-muted-foreground">
            Nenhum evento ainda. Crie o primeiro acima ✨
          </p>
        )}
        {eventos.map((ev) => {
          const foto = mediaUrl(ev.fotoUrl);
          return (
            <Card key={ev.id} className="overflow-hidden rounded-2xl border-border">
              <CardContent className="flex items-center gap-4 p-3 sm:p-4">
                <div className="grid size-16 flex-none place-items-center overflow-hidden rounded-xl bg-lilac text-plum/50">
                  {foto ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={foto} alt="" className="size-full object-cover" />
                  ) : (
                    <ImageIcon className="size-6" />
                  )}
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2">
                    <span className="truncate font-medium">{ev.nome}</span>
                    <Badge variant="secondary" className="flex-none">
                      {ev.status}
                    </Badge>
                    {!ev.dono && (
                      <Badge className="flex-none border-lilac-line bg-lilac text-plum">
                        Co-gestão
                      </Badge>
                    )}
                  </div>
                  <p className="truncate text-sm text-muted-foreground">/e/{ev.slug}</p>
                  {ev.dataEvento && (
                    <p className="mt-0.5 flex items-center gap-1 text-xs text-muted-foreground">
                      <CalendarDays className="size-3" />
                      {new Date(ev.dataEvento + "T00:00:00").toLocaleDateString("pt-BR")}
                    </p>
                  )}
                </div>
                <div className="flex flex-none flex-col gap-2 sm:flex-row">
                  <Link
                    href={`/e/${ev.slug}`}
                    target="_blank"
                    className={
                      buttonVariants({ variant: "outline", size: "sm" }) + " h-9 gap-1.5"
                    }
                  >
                    <ExternalLink className="size-3.5" /> Página
                  </Link>
                  <Link
                    href={`/admin/eventos/${ev.id}`}
                    className={
                      buttonVariants({ size: "sm" }) +
                      " brand-gradient h-9 gap-1.5 border-0 text-white hover:opacity-95"
                    }
                  >
                    <Settings className="size-3.5" /> Gerenciar
                  </Link>
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>
    </main>
  );
}
