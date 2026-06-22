"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { toast } from "sonner";
import { Pencil, Plus, Trash2, Upload, Download, ExternalLink } from "lucide-react";
import { api, ApiError, API_BASE, mediaUrl } from "@/lib/api";
import type { ItemAdmin, ImportResultado } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Modal } from "@/components/Modal";
import { GiftPlaceholder } from "@/components/GiftPlaceholder";

type Form = {
  id?: string;
  nome: string;
  variante: string;
  quantidade: number;
  linkProduto: string;
  imagemUrl: string;
};

const VAZIO: Form = { nome: "", variante: "", quantidade: 1, linkProduto: "", imagemUrl: "" };

export function GestaoPresentes({
  token,
  eventoId,
  onExpirado,
}: {
  token: string;
  eventoId: string;
  onExpirado: () => void;
}) {
  const [itens, setItens] = useState<ItemAdmin[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [form, setForm] = useState<Form | null>(null);
  const [salvando, setSalvando] = useState(false);
  const [remover, setRemover] = useState<ItemAdmin | null>(null);
  const [removerTodos, setRemoverTodos] = useState(false);

  const carregar = useCallback(async () => {
    try {
      setItens(await api.get<ItemAdmin[]>(`/events/${eventoId}/gifts`, token));
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) return onExpirado();
      toast.error("Erro ao carregar presentes");
    } finally {
      setCarregando(false);
    }
  }, [eventoId, token, onExpirado]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    if (!form) return;
    setSalvando(true);
    const corpo = {
      nome: form.nome,
      variante: form.variante || null,
      quantidade: Number(form.quantidade) || 1,
      linkProduto: form.linkProduto || null,
      imagemUrl: form.imagemUrl || null,
    };
    try {
      if (form.id) {
        await api.put(`/events/${eventoId}/gifts/${form.id}`, corpo, token);
        toast.success("Presente atualizado!");
      } else {
        await api.post(`/events/${eventoId}/gifts`, corpo, token);
        toast.success("Presente adicionado!");
      }
      setForm(null);
      await carregar();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro ao salvar");
    } finally {
      setSalvando(false);
    }
  }

  async function confirmarRemover() {
    if (!remover) return;
    try {
      await api.del(`/events/${eventoId}/gifts/${remover.id}`, token);
      toast.success("Presente removido.");
      setRemover(null);
      await carregar();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro ao remover");
    }
  }

  async function confirmarRemoverTodos() {
    try {
      const r = await api.del<{ removidos: number }>(`/events/${eventoId}/gifts`, token);
      toast.success(`${r?.removidos ?? 0} presente(s) removido(s).`);
      setRemoverTodos(false);
      await carregar();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro ao limpar a lista");
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-muted-foreground">
          {itens.length} presente(s) na lista.
        </p>
        <div className="flex gap-2">
          {itens.length > 0 && (
            <Button
              variant="outline"
              onClick={() => setRemoverTodos(true)}
              className="h-10 text-muted-foreground hover:border-rose hover:text-rose"
            >
              <Trash2 className="size-4" /> Excluir todos
            </Button>
          )}
          <Button
            onClick={() => setForm({ ...VAZIO })}
            className="brand-gradient h-10 border-0 text-white hover:opacity-95"
          >
            <Plus className="size-4" /> Adicionar presente
          </Button>
        </div>
      </div>

      {carregando ? (
        <p className="py-8 text-center text-muted-foreground">Carregando...</p>
      ) : itens.length === 0 ? (
        <ImportarPresentes token={token} eventoId={eventoId} onConcluir={carregar} vazio />
      ) : (
        <>
          <div className="space-y-2.5">
            {itens.map((item) => {
              const img = mediaUrl(item.imagemUrl);
              const esgotado = item.status === "SOLD_OUT";
              return (
                <Card key={item.id} className="overflow-hidden rounded-2xl border-border">
                  <CardContent className="flex items-center gap-3 p-3">
                    <div className="size-14 flex-none overflow-hidden rounded-xl">
                      {img ? (
                        // eslint-disable-next-line @next/next/no-img-element
                        <img src={img} alt="" className="size-full object-cover" />
                      ) : (
                        <GiftPlaceholder className="size-full" />
                      )}
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <p className="truncate font-medium">{item.nome}</p>
                        {esgotado && (
                          <span className="flex-none rounded-full bg-muted px-2 py-0.5 text-xs text-muted-foreground">
                            Completo
                          </span>
                        )}
                      </div>
                      {item.variante && (
                        <p className="truncate text-xs text-muted-foreground">{item.variante}</p>
                      )}
                      <p className="mt-0.5 text-xs text-muted-foreground">
                        <span className="font-medium text-plum">{item.reservados}</span> de{" "}
                        {item.quantidadeTotal} reservado(s)
                        {item.linkProduto && (
                          <>
                            {" · "}
                            <a
                              href={item.linkProduto}
                              target="_blank"
                              rel="noreferrer"
                              className="inline-flex items-center gap-0.5 text-rose transition-colors hover:text-plum"
                            >
                              link <ExternalLink className="size-2.5" />
                            </a>
                          </>
                        )}
                      </p>
                    </div>
                    <div className="flex flex-none gap-1">
                      <button
                        type="button"
                        aria-label="Editar"
                        onClick={() =>
                          setForm({
                            id: item.id,
                            nome: item.nome,
                            variante: item.variante ?? "",
                            quantidade: item.quantidadeTotal,
                            linkProduto: item.linkProduto ?? "",
                            imagemUrl: item.imagemUrl ?? "",
                          })
                        }
                        className="grid cursor-pointer place-items-center rounded-lg p-2 text-muted-foreground transition-colors hover:bg-lilac hover:text-plum"
                      >
                        <Pencil className="size-4" />
                      </button>
                      <button
                        type="button"
                        aria-label="Remover"
                        onClick={() => setRemover(item)}
                        className="grid cursor-pointer place-items-center rounded-lg p-2 text-muted-foreground transition-colors hover:bg-rose-50 hover:text-rose"
                      >
                        <Trash2 className="size-4" />
                      </button>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
          <ImportarPresentes token={token} eventoId={eventoId} onConcluir={carregar} />
        </>
      )}

      {/* Modal adicionar/editar */}
      <Modal open={!!form} onClose={() => !salvando && setForm(null)}>
        {form && (
          <form onSubmit={salvar} className="space-y-4">
            <h3 className="font-serif text-2xl">
              {form.id ? "Editar presente" : "Novo presente"}
            </h3>
            <div className="space-y-2">
              <Label htmlFor="g-nome">Nome</Label>
              <Input
                id="g-nome"
                className="h-11"
                value={form.nome}
                onChange={(e) => setForm({ ...form, nome: e.target.value })}
                required
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label htmlFor="g-var">Variante (opcional)</Label>
                <Input
                  id="g-var"
                  className="h-11"
                  placeholder="cor, tamanho..."
                  value={form.variante}
                  onChange={(e) => setForm({ ...form, variante: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="g-qtd">Quantidade</Label>
                <Input
                  id="g-qtd"
                  type="number"
                  min={1}
                  className="h-11"
                  value={form.quantidade}
                  onChange={(e) => setForm({ ...form, quantidade: Number(e.target.value) })}
                  required
                />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="g-link">Link do produto (opcional)</Label>
              <Input
                id="g-link"
                className="h-11"
                placeholder="https://loja.com/produto"
                value={form.linkProduto}
                onChange={(e) => setForm({ ...form, linkProduto: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="g-img">Imagem (URL, opcional)</Label>
              <Input
                id="g-img"
                className="h-11"
                placeholder="https://.../foto.jpg"
                value={form.imagemUrl}
                onChange={(e) => setForm({ ...form, imagemUrl: e.target.value })}
              />
            </div>
            <div className="flex gap-2 pt-1">
              <Button
                type="button"
                variant="outline"
                className="h-11 flex-1"
                onClick={() => setForm(null)}
                disabled={salvando}
              >
                Cancelar
              </Button>
              <Button
                type="submit"
                className="brand-gradient h-11 flex-1 border-0 text-white hover:opacity-95"
                disabled={salvando}
              >
                {salvando ? "Salvando..." : "Salvar"}
              </Button>
            </div>
          </form>
        )}
      </Modal>

      {/* Confirmar remoção */}
      <Modal open={!!remover} onClose={() => setRemover(null)}>
        <div className="space-y-4 text-center">
          <div className="mx-auto grid size-12 place-items-center rounded-2xl bg-rose-50 text-rose">
            <Trash2 className="size-6" />
          </div>
          <div>
            <h3 className="font-serif text-2xl">Remover presente</h3>
            <p className="mt-1 text-sm text-muted-foreground">
              Remover <strong>{remover?.nome}</strong> da lista?
            </p>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" className="h-11 flex-1" onClick={() => setRemover(null)}>
              Cancelar
            </Button>
            <Button
              className="h-11 flex-1 border-0 bg-rose text-white hover:opacity-90"
              onClick={confirmarRemover}
            >
              Remover
            </Button>
          </div>
        </div>
      </Modal>

      {/* Confirmar remoção de todos */}
      <Modal open={removerTodos} onClose={() => setRemoverTodos(false)}>
        <div className="space-y-4 text-center">
          <div className="mx-auto grid size-12 place-items-center rounded-2xl bg-rose-50 text-rose">
            <Trash2 className="size-6" />
          </div>
          <div>
            <h3 className="font-serif text-2xl">Excluir todos os presentes</h3>
            <p className="mt-1 text-sm text-muted-foreground">
              Isso apaga os <strong>{itens.length}</strong> presentes e as reservas feitas
              neles. Não dá para desfazer.
            </p>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" className="h-11 flex-1" onClick={() => setRemoverTodos(false)}>
              Cancelar
            </Button>
            <Button
              className="h-11 flex-1 border-0 bg-rose text-white hover:opacity-90"
              onClick={confirmarRemoverTodos}
            >
              Excluir todos
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}

function ImportarPresentes({
  token,
  eventoId,
  onConcluir,
  vazio,
}: {
  token: string;
  eventoId: string;
  onConcluir: () => void;
  vazio?: boolean;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [enviando, setEnviando] = useState(false);

  async function baixar() {
    const resp = await fetch(`${API_BASE}/events/${eventoId}/gifts/template`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const blob = await resp.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "modelo-presentes.csv";
    a.click();
    URL.revokeObjectURL(url);
  }

  async function enviar() {
    const arquivo = inputRef.current?.files?.[0];
    if (!arquivo) return toast.error("Selecione um arquivo CSV/XLSX.");
    const fd = new FormData();
    fd.append("file", arquivo);
    setEnviando(true);
    try {
      const r = await api.upload<ImportResultado>(`/events/${eventoId}/gifts/import`, fd, token);
      toast.success(`${r.ok} importado(s), ${r.falhas} falha(s) de ${r.total}.`);
      if (inputRef.current) inputRef.current.value = "";
      onConcluir();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Falha na importação");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Card className="rounded-2xl border-dashed border-border bg-white/50">
      <CardContent className="p-5">
        <p className="mb-3 text-sm font-medium">
          {vazio ? "Nenhum presente ainda — importe vários de uma vez" : "Importar em massa (CSV/XLSX)"}
        </p>
        <div className="flex flex-wrap items-center gap-2">
          <Button variant="outline" size="sm" className="h-9 gap-1.5" onClick={baixar}>
            <Download className="size-3.5" /> Baixar modelo
          </Button>
          <Input ref={inputRef} type="file" accept=".csv,.xlsx" className="h-9 max-w-[220px]" />
          <Button
            size="sm"
            className="brand-gradient h-9 gap-1.5 border-0 text-white hover:opacity-95"
            onClick={enviar}
            disabled={enviando}
          >
            <Upload className="size-3.5" /> {enviando ? "Enviando..." : "Importar"}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
