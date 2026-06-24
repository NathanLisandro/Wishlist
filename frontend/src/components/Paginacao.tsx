"use client";

import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";

/** Controle de paginação simples (anterior/próxima) para listas do admin. */
export function Paginacao({
  page,
  total,
  tam,
  onPage,
}: {
  page: number;
  total: number;
  tam: number;
  onPage: (p: number) => void;
}) {
  const totalPaginas = Math.max(1, Math.ceil(total / tam));
  if (totalPaginas <= 1) return null;

  return (
    <div className="flex items-center justify-between gap-3 pt-1">
      <Button
        variant="outline"
        size="sm"
        className="h-9"
        onClick={() => onPage(page - 1)}
        disabled={page <= 0}
      >
        <ChevronLeft className="size-4" /> Anterior
      </Button>
      <span className="text-sm text-muted-foreground">
        Página {page + 1} de {totalPaginas}
      </span>
      <Button
        variant="outline"
        size="sm"
        className="h-9"
        onClick={() => onPage(page + 1)}
        disabled={page + 1 >= totalPaginas}
      >
        Próxima <ChevronRight className="size-4" />
      </Button>
    </div>
  );
}
