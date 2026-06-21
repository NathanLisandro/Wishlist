import Link from "next/link";
import { Heart, CalendarCheck, Gift, BarChart3 } from "lucide-react";
import { buttonVariants } from "@/components/ui/button";

const recursos = [
  {
    icon: CalendarCheck,
    titulo: "Confirmação de presença",
    texto: "Convidados confirmam pelo telefone, com acompanhantes — sem cadastro.",
  },
  {
    icon: Gift,
    titulo: "Lista de presentes",
    texto: "Presentes físicos ou em dinheiro via Mercado Pago, sem repetições.",
  },
  {
    icon: BarChart3,
    titulo: "Painel em tempo real",
    texto: "Acompanhe presenças, presentes e quem está acessando agora.",
  },
];

export default function Home() {
  return (
    <main className="mx-auto flex w-full max-w-3xl flex-1 flex-col items-center justify-center gap-10 px-6 py-16 text-center">
      <div className="space-y-5">
        <span className="brand-gradient mx-auto grid size-16 place-items-center rounded-3xl text-white shadow-[0_18px_40px_-16px_rgba(142,43,87,0.6)]">
          <Gift className="size-8" />
        </span>
        <h1 className="font-serif text-[clamp(40px,9vw,64px)] leading-[1.02] tracking-tight">
          Sua lista de presentes,
          <br />
          <span className="italic text-plum">do jeito certo</span>
        </h1>
        <p className="mx-auto max-w-xl text-lg text-muted-foreground">
          Casamento, chá de panela ou casa nova: monte a lista, compartilhe um link
          lindo e acompanhe presenças e presentes em um só lugar.
        </p>
      </div>

      <div className="flex w-full max-w-md flex-col gap-3 sm:flex-row sm:justify-center">
        <Link
          href="/admin/register"
          className={
            buttonVariants({ size: "lg" }) +
            " brand-gradient h-12 border-0 px-6 text-base text-white shadow-[0_14px_26px_-12px_rgba(142,43,87,0.6)] hover:opacity-95"
          }
        >
          Começar agora
        </Link>
        <Link
          href="/admin/login"
          className={buttonVariants({ variant: "outline", size: "lg" }) + " h-12 px-6 text-base"}
        >
          Já tenho conta
        </Link>
      </div>

      <div className="grid w-full gap-4 sm:grid-cols-3">
        {recursos.map((r) => (
          <div
            key={r.titulo}
            className="rounded-2xl border border-border bg-white/70 p-5 text-left backdrop-blur"
          >
            <span className="grid size-10 place-items-center rounded-xl bg-lilac text-plum">
              <r.icon className="size-5" />
            </span>
            <h3 className="mt-3 font-medium">{r.titulo}</h3>
            <p className="mt-1 text-sm text-muted-foreground">{r.texto}</p>
          </div>
        ))}
      </div>

      <p className="flex items-center gap-1.5 text-sm text-muted-foreground">
        <Heart className="size-4 text-rose" /> feito para o seu grande dia
      </p>
    </main>
  );
}
