"use client";

import { useRef, useState } from "react";
import { toast } from "sonner";
import { Sparkles, FileUp, Type, Link as LinkIcon, Trash2, Check } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import type { ItemExtraido, ImportResultado } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Modal } from "@/components/Modal";

type Modo = "arquivo" | "texto" | "link";

const fmtBRL = (v: number) =>
  new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(v);

/** Importa uma lista de presentes já existente do casal usando IA (foto/PDF/texto/link). */
export function ImportarComIa({
  token,
  eventoId,
  onConcluir,
}: {
  token: string;
  eventoId: string;
  onConcluir: () => void;
}) {
  const fileRef = useRef<HTMLInputElement>(null);
  const [open, setOpen] = useState(false);
  const [modo, setModo] = useState<Modo>("arquivo");
  const [texto, setTexto] = useState("");
  const [link, setLink] = useState("");
  const [estimar, setEstimar] = useState(true);
  const [extraindo, setExtraindo] = useState(false);
  const [importando, setImportando] = useState(false);
  const [itens, setItens] = useState<ItemExtraido[] | null>(null);

  function fechar() {
    if (extraindo || importando) return;
    setOpen(false);
    setItens(null);
    setTexto("");
    setLink("");
    if (fileRef.current) fileRef.current.value = "";
  }

  async function extrairArquivo(e: React.ChangeEvent<HTMLInputElement>) {
    const arquivo = e.target.files?.[0];
    if (!arquivo) return;
    if (arquivo.size > 8 * 1024 * 1024) {
      toast.error("Arquivo muito grande (máx. 8MB).");
      return;
    }
    const fd = new FormData();
    fd.append("file", arquivo);
    setExtraindo(true);
    try {
      const r = await api.upload<ItemExtraido[]>(
        `/events/${eventoId}/gifts/ai-extract?estimar=${estimar}`,
        fd,
        token,
      );
      receber(r);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Falha ao ler com IA");
    } finally {
      setExtraindo(false);
      if (fileRef.current) fileRef.current.value = "";
    }
  }

  async function extrairTexto() {
    const corpo = modo === "link" ? { link, estimar } : { texto, estimar };
    if (modo === "texto" && !texto.trim()) return toast.error("Cole o texto da lista.");
    if (modo === "link" && !link.trim()) return toast.error("Informe o link da lista.");
    setExtraindo(true);
    try {
      const r = await api.post<ItemExtraido[]>(
        `/events/${eventoId}/gifts/ai-extract-text`,
        corpo,
        token,
      );
      receber(r);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Falha ao ler com IA");
    } finally {
      setExtraindo(false);
    }
  }

  function receber(r: ItemExtraido[]) {
    if (!r.length) {
      toast.error("A IA não encontrou itens. Tente outra foto/texto.");
      return;
    }
    setItens(r);
    toast.success(`${r.length} item(ns) encontrado(s). Revise antes de importar.`);
  }

  async function importar() {
    if (!itens || !itens.length) return;
    setImportando(true);
    try {
      const r = await api.post<ImportResultado>(
        `/events/${eventoId}/gifts/ai-import`,
        { itens },
        token,
      );
      toast.success(`${r.ok} importado(s), ${r.falhas} falha(s) de ${r.total}.`);
      fechar();
      onConcluir();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Falha ao importar");
    } finally {
      setImportando(false);
    }
  }

  return (
    <>
      <Button
        variant="outline"
        onClick={() => setOpen(true)}
        className="h-10 border-plum/30 text-plum hover:bg-lilac"
      >
        <Sparkles className="size-4" /> Importar com IA
      </Button>

      <Modal open={open} onClose={fechar}>
        <div className="space-y-4">
          <div className="flex items-center gap-2">
            <span className="grid size-10 place-items-center rounded-xl bg-lilac text-plum">
              <Sparkles className="size-5" />
            </span>
            <div>
              <h3 className="font-serif text-2xl">Importar com IA</h3>
              <p className="text-xs text-muted-foreground">
                Já tem uma lista pronta? Envie e a IA cria os presentes.
              </p>
            </div>
          </div>

          {!itens ? (
            <>
              <div className="flex flex-wrap gap-2">
                {(
                  [
                    { id: "arquivo", nome: "Foto / PDF", Icon: FileUp },
                    { id: "texto", nome: "Texto", Icon: Type },
                    { id: "link", nome: "Link", Icon: LinkIcon },
                  ] as const
                ).map((m) => (
                  <button
                    key={m.id}
                    type="button"
                    onClick={() => setModo(m.id)}
                    className={
                      "flex items-center gap-1.5 rounded-xl border px-3 py-2 text-sm transition-colors " +
                      (modo === m.id
                        ? "border-plum bg-lilac"
                        : "border-border bg-white hover:bg-lilac/50")
                    }
                  >
                    <m.Icon className="size-4" /> {m.nome}
                  </button>
                ))}
              </div>

              {modo === "arquivo" && (
                <div className="space-y-2">
                  <input
                    ref={fileRef}
                    type="file"
                    accept="image/*,application/pdf"
                    capture="environment"
                    className="hidden"
                    onChange={extrairArquivo}
                  />
                  <Button
                    type="button"
                    variant="outline"
                    className="h-11 w-full"
                    onClick={() => fileRef.current?.click()}
                    disabled={extraindo}
                  >
                    <FileUp className="size-4" />
                    {extraindo ? "Lendo..." : "Escolher foto ou PDF"}
                  </Button>
                  <p className="text-xs text-muted-foreground">
                    Print de outro site, foto de uma lista no papel, PDF ou planilha. Máx. 8MB.
                  </p>
                </div>
              )}

              {modo === "texto" && (
                <div className="space-y-2">
                  <Label htmlFor="ia-texto">Cole a lista</Label>
                  <Textarea
                    id="ia-texto"
                    rows={6}
                    placeholder={"Geladeira\nJogo de panelas inox\n2 jogos de cama king..."}
                    value={texto}
                    onChange={(e) => setTexto(e.target.value)}
                  />
                </div>
              )}

              {modo === "link" && (
                <div className="space-y-2">
                  <Label htmlFor="ia-link">Link da lista (outro site)</Label>
                  <Input
                    id="ia-link"
                    className="h-11"
                    placeholder="https://..."
                    value={link}
                    onChange={(e) => setLink(e.target.value)}
                  />
                </div>
              )}

              <label className="flex cursor-pointer items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={estimar}
                  onChange={(e) => setEstimar(e.target.checked)}
                  className="size-4 accent-plum"
                />
                Estimar preço de cada item (para o ticket médio)
              </label>

              {modo !== "arquivo" && (
                <Button
                  type="button"
                  className="brand-gradient h-11 w-full border-0 text-white hover:opacity-95"
                  onClick={extrairTexto}
                  disabled={extraindo}
                >
                  <Sparkles className="size-4" /> {extraindo ? "Lendo..." : "Ler com IA"}
                </Button>
              )}
            </>
          ) : (
            <>
              <p className="text-sm text-muted-foreground">
                {itens.length} item(ns). Remova o que não quiser e confirme.
              </p>
              <ul className="max-h-72 space-y-1.5 overflow-y-auto">
                {itens.map((it, idx) => (
                  <li
                    key={idx}
                    className="flex items-center gap-2 rounded-xl border border-border p-2.5"
                  >
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-medium">
                        {it.quantidade && it.quantidade > 1 ? `${it.quantidade}× ` : ""}
                        {it.nome}
                      </p>
                      <p className="truncate text-xs text-muted-foreground">
                        {it.variante ?? ""}
                        {it.precoEstimado != null &&
                          `${it.variante ? " · " : ""}~${fmtBRL(it.precoEstimado)}`}
                      </p>
                    </div>
                    <button
                      type="button"
                      aria-label="Remover"
                      onClick={() => setItens((arr) => (arr ?? []).filter((_, i) => i !== idx))}
                      className="grid cursor-pointer place-items-center rounded-lg p-1.5 text-muted-foreground transition-colors hover:bg-rose-50 hover:text-rose"
                    >
                      <Trash2 className="size-4" />
                    </button>
                  </li>
                ))}
              </ul>
              <div className="flex gap-2">
                <Button
                  type="button"
                  variant="outline"
                  className="h-11 flex-1"
                  onClick={() => setItens(null)}
                  disabled={importando}
                >
                  Voltar
                </Button>
                <Button
                  type="button"
                  className="brand-gradient h-11 flex-1 border-0 text-white hover:opacity-95"
                  onClick={importar}
                  disabled={importando || itens.length === 0}
                >
                  <Check className="size-4" />
                  {importando ? "Importando..." : `Importar ${itens.length}`}
                </Button>
              </div>
            </>
          )}
        </div>
      </Modal>
    </>
  );
}
