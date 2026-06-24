"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { toast } from "sonner";
import {
  UserPlus, Upload, Download, Check, X, Clock, Users, Pencil, Trash2, Phone,
} from "lucide-react";
import { api, ApiError, API_BASE } from "@/lib/api";
import { formatarTelefoneBR, apenasDigitos, exibirTelefone } from "@/lib/format";
import type { ConvidadoPainel, ImportResultado, Pagina } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Modal } from "@/components/Modal";
import { Paginacao } from "@/components/Paginacao";

const TAM_PAGINA = 20;

const STATUS: Record<string, { label: string; classe: string; Icon: typeof Check }> = {
  CONFIRMED: { label: "Confirmado", classe: "bg-emerald-50 text-emerald-600", Icon: Check },
  DECLINED: { label: "Não vai", classe: "bg-muted text-muted-foreground", Icon: X },
  CANCELLED: { label: "Não vai", classe: "bg-muted text-muted-foreground", Icon: X },
  PENDING: { label: "Pendente", classe: "bg-amber-50 text-amber-600", Icon: Clock },
};

type Form = { id?: string; nome: string; telefone: string; acompanhantes: number };

/** Converte o E.164 vindo da API no formato editável "(67) 99843-7332". */
function paraEdicao(e164: string): string {
  const nacional = e164.replace(/\D/g, "").replace(/^55/, "");
  return formatarTelefoneBR(nacional);
}

export function GestaoConvidados({
  token,
  eventoId,
  onExpirado,
}: {
  token: string;
  eventoId: string;
  onExpirado: () => void;
}) {
  const [convidados, setConvidados] = useState<ConvidadoPainel[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [carregando, setCarregando] = useState(true);
  const [form, setForm] = useState<Form | null>(null);
  const [salvando, setSalvando] = useState(false);
  const [remover, setRemover] = useState<ConvidadoPainel | null>(null);
  const [removerTodos, setRemoverTodos] = useState(false);

  const carregar = useCallback(async () => {
    try {
      const p = await api.get<Pagina<ConvidadoPainel>>(
        `/events/${eventoId}/guests?page=${page}&size=${TAM_PAGINA}`,
        token,
      );
      setConvidados(p.itens);
      setTotal(p.total);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) return onExpirado();
      toast.error("Erro ao carregar convidados");
    } finally {
      setCarregando(false);
    }
  }, [eventoId, token, onExpirado, page]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    if (!form) return;
    const digitos = apenasDigitos(form.telefone);
    if (digitos.length < 10) {
      toast.error("Informe o DDD + número (ex.: 11 99999-9999).");
      return;
    }
    setSalvando(true);
    const corpo = {
      nomeCompleto: form.nome,
      telefone: `+55${digitos}`,
      acompanhantesPermitidos: Number(form.acompanhantes) || 0,
    };
    try {
      if (form.id) {
        await api.put(`/events/${eventoId}/guests/${form.id}`, corpo, token);
        toast.success("Convidado atualizado!");
      } else {
        await api.post(`/events/${eventoId}/guests`, corpo, token);
        toast.success("Convidado adicionado!");
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
      await api.del(`/events/${eventoId}/guests/${remover.id}`, token);
      toast.success("Convidado removido.");
      const voltarPagina = convidados.length === 1 && page > 0;
      setRemover(null);
      if (voltarPagina) setPage((p) => p - 1);
      else await carregar();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro ao remover");
    }
  }

  async function confirmarRemoverTodos() {
    try {
      const r = await api.del<{ removidos: number }>(`/events/${eventoId}/guests`, token);
      toast.success(`${r?.removidos ?? 0} convidado(s) removido(s).`);
      setRemoverTodos(false);
      if (page === 0) await carregar();
      else setPage(0);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro ao limpar a lista");
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-muted-foreground">{total} convidado(s)</p>
        <div className="flex gap-2">
          {total > 0 && (
            <Button
              variant="outline"
              onClick={() => setRemoverTodos(true)}
              className="h-10 text-muted-foreground hover:border-rose hover:text-rose"
            >
              <Trash2 className="size-4" /> Excluir todos
            </Button>
          )}
          <Button
            onClick={() => setForm({ nome: "", telefone: "", acompanhantes: 0 })}
            className="brand-gradient h-10 border-0 text-white hover:opacity-95"
          >
            <UserPlus className="size-4" /> Adicionar convidado
          </Button>
        </div>
      </div>

      {carregando ? (
        <p className="py-8 text-center text-muted-foreground">Carregando...</p>
      ) : total === 0 ? (
        <ImportarConvidados token={token} eventoId={eventoId} onConcluir={carregar} vazio />
      ) : (
        <>
          <Card className="overflow-hidden rounded-2xl border-border">
            <CardContent className="p-0">
              <ul className="divide-y divide-border">
                {convidados.map((c) => {
                  const s = STATUS[c.statusRsvp] ?? STATUS.PENDING;
                  return (
                    <li key={c.id} className="flex items-center gap-3 p-3 sm:px-4">
                      <span className="grid size-9 flex-none place-items-center rounded-full bg-lilac text-xs font-semibold text-plum">
                        {c.nome
                          .split(" ")
                          .filter(Boolean)
                          .slice(0, 2)
                          .map((p) => p[0]?.toUpperCase())
                          .join("")}
                      </span>
                      <div className="min-w-0 flex-1">
                        <p className="truncate font-medium">{c.nome}</p>
                        <p className="flex items-center gap-1 text-xs text-muted-foreground">
                          <Phone className="size-3" /> {exibirTelefone(c.telefone)}
                          <span className="text-border">·</span>
                          {c.statusRsvp === "CONFIRMED"
                            ? `+${c.acompanhantesConfirmados} acompanhante(s)`
                            : `até ${c.acompanhantesPermitidos}`}
                        </p>
                      </div>
                      <span
                        className={`hidden flex-none items-center gap-1 rounded-full px-2.5 py-1 text-xs font-medium sm:flex ${s.classe}`}
                      >
                        <s.Icon className="size-3" /> {s.label}
                      </span>
                      <div className="flex flex-none gap-1">
                        <button
                          type="button"
                          aria-label="Editar convidado"
                          onClick={() =>
                            setForm({
                              id: c.id,
                              nome: c.nome,
                              telefone: paraEdicao(c.telefone),
                              acompanhantes: c.acompanhantesPermitidos,
                            })
                          }
                          className="grid cursor-pointer place-items-center rounded-lg p-2 text-muted-foreground transition-colors hover:bg-lilac hover:text-plum"
                        >
                          <Pencil className="size-4" />
                        </button>
                        <button
                          type="button"
                          aria-label="Remover convidado"
                          onClick={() => setRemover(c)}
                          className="grid cursor-pointer place-items-center rounded-lg p-2 text-muted-foreground transition-colors hover:bg-rose-50 hover:text-rose"
                        >
                          <Trash2 className="size-4" />
                        </button>
                      </div>
                    </li>
                  );
                })}
              </ul>
            </CardContent>
          </Card>
          <Paginacao page={page} total={total} tam={TAM_PAGINA} onPage={setPage} />
          <ImportarConvidados token={token} eventoId={eventoId} onConcluir={carregar} />
        </>
      )}

      {/* Modal adicionar/editar */}
      <Modal open={!!form} onClose={() => !salvando && setForm(null)}>
        {form && (
          <form onSubmit={salvar} className="space-y-4">
            <div className="flex items-center gap-3">
              <span className="grid size-11 place-items-center rounded-[14px] bg-lilac text-plum">
                <Users className="size-5" />
              </span>
              <h3 className="font-serif text-2xl">{form.id ? "Editar convidado" : "Novo convidado"}</h3>
            </div>
            <div className="space-y-2">
              <Label htmlFor="c-nome">Nome completo</Label>
              <Input
                id="c-nome"
                className="h-11"
                value={form.nome}
                onChange={(e) => setForm({ ...form, nome: e.target.value })}
                required
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label htmlFor="c-tel">Telefone (DDD + número)</Label>
                <div className="flex items-stretch overflow-hidden rounded-lg border border-input bg-white focus-within:border-ring focus-within:ring-2 focus-within:ring-ring/30">
                  <span className="grid place-items-center border-r border-input bg-lilac px-2.5 text-sm font-medium text-plum">
                    +55
                  </span>
                  <input
                    id="c-tel"
                    type="tel"
                    inputMode="numeric"
                    className="h-11 flex-1 bg-transparent px-3 text-sm outline-none"
                    placeholder="(11) 99999-9999"
                    value={form.telefone}
                    onChange={(e) => setForm({ ...form, telefone: formatarTelefoneBR(e.target.value) })}
                    required
                  />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="c-ac">Acompanhantes</Label>
                <Input
                  id="c-ac"
                  type="number"
                  min={0}
                  className="h-11"
                  value={form.acompanhantes}
                  onChange={(e) => setForm({ ...form, acompanhantes: Number(e.target.value) })}
                />
              </div>
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

      {/* Confirmar remoção de um */}
      <Modal open={!!remover} onClose={() => setRemover(null)}>
        <div className="space-y-4 text-center">
          <div className="mx-auto grid size-12 place-items-center rounded-2xl bg-rose-50 text-rose">
            <Trash2 className="size-6" />
          </div>
          <div>
            <h3 className="font-serif text-2xl">Remover convidado</h3>
            <p className="mt-1 text-sm text-muted-foreground">
              Remover <strong>{remover?.nome}</strong> da lista? As reservas e a presença dele(a)
              também serão apagadas.
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
            <h3 className="font-serif text-2xl">Excluir todos os convidados</h3>
            <p className="mt-1 text-sm text-muted-foreground">
              Isso apaga os <strong>{total}</strong> convidados e suas
              reservas/presenças. Não dá para desfazer.
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

function ImportarConvidados({
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
    const resp = await fetch(`${API_BASE}/events/${eventoId}/guests/template`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const blob = await resp.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "modelo-convidados.csv";
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
      const r = await api.upload<ImportResultado>(`/events/${eventoId}/guests/import`, fd, token);
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
          {vazio
            ? "Nenhum convidado ainda — importe sua lista de uma vez"
            : "Importar em massa (CSV/XLSX)"}
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
