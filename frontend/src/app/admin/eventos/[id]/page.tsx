"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { toast } from "sonner";
import { ArrowLeft, ExternalLink, LinkIcon } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { getAdminToken, clearAdmin } from "@/lib/auth";
import {
  buscarMe,
  concluirOnboarding,
  marcarTourVisto,
  onboardingConcluidoLocal,
  tourVisto,
} from "@/lib/onboarding";
import { SpotlightTour } from "@/components/onboarding/SpotlightTour";
import { PASSOS_GERENCIAR } from "@/components/onboarding/passos";
import type { EventoDetalhe } from "@/lib/types";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Brand } from "@/components/Brand";
import { VisaoGeral } from "./VisaoGeral";
import { GestaoPresentes } from "./GestaoPresentes";
import { GestaoConvidados } from "./GestaoConvidados";
import { Personalizacao } from "./Personalizacao";
import { Pagamento } from "./Pagamento";
import { CoGestao } from "./CoGestao";

export default function GerenciarEventoPage() {
  const router = useRouter();
  const params = useParams<{ id: string }>();
  const eventoId = params.id;
  const [token, setToken] = useState<string | null>(null);
  const [detalhe, setDetalhe] = useState<EventoDetalhe | null>(null);
  const [mostrarTour, setMostrarTour] = useState(false);
  const tourChecado = useRef(false);

  const expirado = useCallback(() => {
    clearAdmin();
    router.replace("/admin/login");
  }, [router]);

  const carregar = useCallback(
    async (t: string) => {
      try {
        setDetalhe(await api.get<EventoDetalhe>(`/events/${eventoId}`, t));
      } catch (err) {
        if (err instanceof ApiError && err.status === 401) expirado();
        else toast.error("Erro ao carregar o evento");
      }
    },
    [eventoId, expirado],
  );

  useEffect(() => {
    const t = getAdminToken();
    if (!t) {
      router.replace("/admin/login");
      return;
    }
    setToken(t);
    carregar(t);
  }, [carregar, router]);

  // Tour de gestão: dispara quando o evento carrega, só para contas novas.
  useEffect(() => {
    if (!detalhe || !token || tourChecado.current) return;
    if (onboardingConcluidoLocal() || tourVisto("gerenciar")) return;
    tourChecado.current = true;
    buscarMe(token).then((me) => {
      if (me && !me.onboardingConcluido) setMostrarTour(true);
    });
  }, [detalhe, token]);

  function encerrarTour() {
    marcarTourVisto("gerenciar");
    setMostrarTour(false);
    if (token) concluirOnboarding(token); // último passo do onboarding
  }

  if (!token) return null;

  function copiarLink() {
    if (!detalhe) return;
    const url = `${window.location.origin}/e/${detalhe.slug}`;
    navigator.clipboard.writeText(url);
    toast.success("Link do evento copiado!");
  }

  return (
    <main className="mx-auto w-full max-w-5xl px-5 py-6 sm:px-6">
      <div className="flex items-center justify-between gap-3">
        <Brand href="/admin/eventos" />
        <div className="flex items-center gap-2">
          {detalhe && (
            <>
              {detalhe.dono && (
                <CoGestao
                  token={token}
                  eventoId={eventoId}
                  tokenInicial={detalhe.tokenConviteGestao}
                  modoSurpresaInicial={detalhe.modoSurpresa}
                  onSurpresaChange={() => carregar(token)}
                />
              )}
              <button
                onClick={copiarLink}
                data-tour="copiar-link"
                className="flex cursor-pointer items-center gap-1.5 rounded-lg border border-border px-3 py-1.5 text-[13px] font-medium text-muted-foreground transition-colors hover:border-lilac-line hover:bg-white hover:text-plum"
              >
                <LinkIcon className="size-3.5" /> Copiar link
              </button>
              <Link
                href={`/e/${detalhe.slug}`}
                target="_blank"
                className="flex items-center gap-1.5 rounded-lg border border-border px-3 py-1.5 text-[13px] font-medium text-muted-foreground transition-colors hover:border-lilac-line hover:bg-white hover:text-plum"
              >
                <ExternalLink className="size-3.5" /> Ver página
              </Link>
            </>
          )}
        </div>
      </div>

      <div className="mt-6 flex items-center gap-3">
        <button
          onClick={() => router.push("/admin/eventos")}
          aria-label="Voltar"
          className="grid cursor-pointer place-items-center rounded-lg border border-border p-2 text-muted-foreground transition-colors hover:bg-lilac hover:text-plum"
        >
          <ArrowLeft className="size-4" />
        </button>
        <h1 className="font-serif text-3xl sm:text-4xl">{detalhe?.nome ?? "Evento"}</h1>
      </div>

      <Tabs defaultValue="visao" className="mt-6">
        <TabsList
          className="flex w-full overflow-x-auto [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
          data-tour="abas"
        >
          <TabsTrigger value="visao" className="flex-none sm:flex-1 cursor-pointer">Visão geral</TabsTrigger>
          {detalhe?.podeGerenciarPresentes !== false && (
            <TabsTrigger value="presentes" className="flex-none sm:flex-1 cursor-pointer" data-tour="aba-presentes">Presentes</TabsTrigger>
          )}
          <TabsTrigger value="convidados" className="flex-none sm:flex-1 cursor-pointer" data-tour="aba-convidados">Convidados</TabsTrigger>
          <TabsTrigger value="personalizar" className="flex-none sm:flex-1 cursor-pointer" data-tour="aba-personalizar">Personalizar</TabsTrigger>
          {detalhe?.dono && (
            <TabsTrigger value="pagamento" className="flex-none sm:flex-1 cursor-pointer" data-tour="aba-pagamento">Pagamento</TabsTrigger>
          )}
        </TabsList>

        <TabsContent value="visao" className="mt-5">
          <VisaoGeral token={token} eventoId={eventoId} onExpirado={expirado} />
        </TabsContent>
        {detalhe?.podeGerenciarPresentes !== false && (
          <TabsContent value="presentes" className="mt-5">
            <GestaoPresentes
              token={token}
              eventoId={eventoId}
              onExpirado={expirado}
              dono={!!detalhe?.dono}
            />
          </TabsContent>
        )}
        <TabsContent value="convidados" className="mt-5">
          <GestaoConvidados token={token} eventoId={eventoId} onExpirado={expirado} />
        </TabsContent>
        <TabsContent value="personalizar" className="mt-5">
          {detalhe ? (
            <Personalizacao token={token} detalhe={detalhe} onSaved={() => carregar(token)} />
          ) : (
            <p className="py-8 text-center text-muted-foreground">Carregando...</p>
          )}
        </TabsContent>
        {detalhe?.dono && (
          <TabsContent value="pagamento" className="mt-5">
            <Pagamento token={token} />
          </TabsContent>
        )}
      </Tabs>

      {mostrarTour && (
        <SpotlightTour
          steps={PASSOS_GERENCIAR}
          onFinish={encerrarTour}
          onSkip={encerrarTour}
        />
      )}
    </main>
  );
}
