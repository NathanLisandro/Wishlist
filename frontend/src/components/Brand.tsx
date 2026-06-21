import Link from "next/link";

/** Marca do produto (monograma em gradiente + nome). Usada nos topos das telas. */
export function Brand({
  href = "/",
  sub,
}: {
  href?: string;
  sub?: string;
}) {
  return (
    <Link href={href} className="flex items-center gap-2.5">
      <span className="brand-gradient grid size-9 place-items-center rounded-[11px] font-serif text-base text-white">
        L
      </span>
      <span className="leading-tight">
        <span className="block text-sm font-medium text-foreground">Lista de Presentes</span>
        {sub && <span className="block text-xs text-muted-foreground">{sub}</span>}
      </span>
    </Link>
  );
}
