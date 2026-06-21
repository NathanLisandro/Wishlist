import type { Metadata } from "next";
import { Calendar, ImageIcon, MapPin, ArrowUpRight } from "lucide-react";
import { api, mediaUrl } from "@/lib/api";

/** URL absoluta da foto p/ o preview (Open Graph) — só quando NEXT_PUBLIC_SITE_URL é definido. */
function fotoAbsoluta(fotoUrl: string | null): string | undefined {
  if (!fotoUrl) return undefined;
  if (/^https?:\/\//i.test(fotoUrl)) return fotoUrl;
  const site = process.env.NEXT_PUBLIC_SITE_URL?.replace(/\/$/, "");
  return site ? `${site}${fotoUrl}` : undefined;
}
import type { EventoPublico } from "@/lib/types";
import { AreaConvidado } from "./AreaConvidado";

async function carregar(slug: string): Promise<EventoPublico | null> {
  try {
    return await api.get<EventoPublico>(`/public/events/${slug}`);
  } catch {
    return null;
  }
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ slug: string }>;
}): Promise<Metadata> {
  const { slug } = await params;
  const ev = await carregar(slug);
  if (!ev) return { title: "Evento não encontrado" };
  const descricao =
    ev.mensagemCasal ??
    ev.descricao ??
    "Veja a lista de presentes e confirme sua presença.";
  const foto = fotoAbsoluta(ev.fotoUrl);
  return {
    title: ev.nome,
    description: descricao,
    openGraph: {
      title: ev.nome,
      description: descricao,
      type: "website",
      images: foto ? [{ url: foto }] : undefined,
    },
  };
}

function nomesComEComercial(nome: string) {
  if (!nome.includes("&")) return nome;
  const [a, b] = nome.split("&");
  return (
    <>
      {a.trim()} <span className="italic opacity-90">&amp;</span> {b.trim()}
    </>
  );
}

export default async function Page({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const evento = await carregar(slug);

  if (!evento) {
    return (
      <main className="flex flex-1 items-center justify-center p-8 text-center">
        <p className="text-muted-foreground">Evento não encontrado.</p>
      </main>
    );
  }

  const dataFmt = evento.dataEvento
    ? new Date(evento.dataEvento + "T00:00:00").toLocaleDateString("pt-BR", {
        day: "2-digit",
        month: "long",
        year: "numeric",
      })
    : null;
  const foto = mediaUrl(evento.fotoUrl);
  const quote = evento.mensagemCasal ?? evento.descricao;

  return (
    <main className="mx-auto w-full max-w-[600px] px-5 pb-16 sm:px-6">
      {/* Herói com foto do casal (ou gradiente como fallback elegante). */}
      <section className="rise relative mt-6 flex h-[360px] items-end overflow-hidden rounded-[26px] shadow-[0_36px_70px_-34px_rgba(110,31,67,0.55)] sm:h-[380px]">
        {foto ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img
            src={foto}
            alt={evento.nome}
            className="absolute inset-0 size-full object-cover"
          />
        ) : (
          <div className="hero-noise absolute inset-0 bg-[radial-gradient(at_18%_22%,#F7B5C4_0,transparent_48%),radial-gradient(at_82%_14%,#F9CC8E_0,transparent_44%),radial-gradient(at_78%_80%,#C5AEE8_0,transparent_50%),radial-gradient(at_22%_82%,#F4A78F_0,transparent_46%),linear-gradient(135deg,#F0A6C2,#E2A0D6)]" />
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-[rgba(44,20,40,0.62)] via-[rgba(44,20,40,0.12)] to-transparent" />

        {evento.categoria && (
          <span className="absolute left-4 top-4 z-[2] rounded-full bg-white/80 px-3 py-1.5 text-[11px] font-semibold uppercase tracking-[0.16em] text-plum backdrop-blur">
            {evento.categoria}
          </span>
        )}
        {!foto && (
          <span className="absolute right-4 top-4 z-[2] flex items-center gap-1.5 rounded-full bg-white/80 px-3 py-1.5 text-xs text-muted-foreground backdrop-blur">
            <ImageIcon className="size-3.5" /> foto do casal
          </span>
        )}

        <div className="relative z-[2] w-full p-7 text-white sm:p-8">
          <p className="text-[13px] font-medium opacity-85">
            {evento.categoria ? "Você está convidado" : "Lista de presentes"}
          </p>
          <h1 className="mt-1 font-serif text-[clamp(44px,11vw,64px)] font-normal leading-[0.98] tracking-[0.01em] drop-shadow-[0_2px_24px_rgba(60,20,45,0.35)]">
            {nomesComEComercial(evento.nome)}
          </h1>
        </div>
      </section>

      {quote && (
        <p className="rise mx-auto mt-7 max-w-[460px] text-center font-serif text-[clamp(20px,5vw,26px)] italic leading-[1.34] text-foreground">
          {quote}
          <span className="mx-auto mt-5 block h-px w-10 bg-lilac-line" />
        </p>
      )}

      {(dataFmt || evento.local) && (
        <div className="rise mt-6 flex flex-col overflow-hidden rounded-[18px] border border-border bg-white/70 shadow-[0_16px_40px_-28px_rgba(110,31,67,0.4)] backdrop-blur sm:flex-row">
          {dataFmt && (
            <div className="flex flex-1 items-center gap-3 p-4 sm:p-[16px_18px]">
              <span className="grid size-9 flex-none place-items-center rounded-[10px] bg-lilac text-plum">
                <Calendar className="size-[17px]" />
              </span>
              <div>
                <div className="text-[11.5px] text-muted-foreground">Quando</div>
                <div className="text-sm font-medium leading-tight">{dataFmt}</div>
              </div>
            </div>
          )}
          {evento.local && (
            <div className="flex flex-1 items-center gap-3 border-t border-border p-4 sm:border-l sm:border-t-0 sm:p-[16px_18px]">
              <span className="grid size-9 flex-none place-items-center rounded-[10px] bg-lilac text-plum">
                <MapPin className="size-[17px]" />
              </span>
              <div>
                <div className="text-[11.5px] text-muted-foreground">Onde</div>
                <div className="text-sm font-medium leading-tight">{evento.local}</div>
                <a
                  href={`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(evento.local)}`}
                  target="_blank"
                  rel="noreferrer"
                  className="mt-0.5 inline-flex items-center gap-1 text-xs font-medium text-rose transition-colors hover:text-plum"
                >
                  Ver no mapa <ArrowUpRight className="size-3" />
                </a>
              </div>
            </div>
          )}
        </div>
      )}

      <div className="mt-7">
        <AreaConvidado evento={evento} />
      </div>

      <p className="mt-8 text-center font-serif text-lg italic text-muted-foreground">
        {evento.mensagemFinal?.trim() || "Mal podemos esperar por você."}
      </p>
    </main>
  );
}
