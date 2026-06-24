"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { toast } from "sonner";
import { Camera, ImageIcon, Save, ExternalLink, RefreshCw, Palette, Trash2 } from "lucide-react";
import { api, ApiError, mediaUrl } from "@/lib/api";
import { PALETAS_DISPONIVEIS, OVERLAY_PADRAO } from "@/lib/temas";
import type { EventoDetalhe } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent } from "@/components/ui/card";

const ANIMACOES: { id: string; nome: string }[] = [
  { id: "SUAVE", nome: "Suave" },
  { id: "CASCATA", nome: "Cascata" },
  { id: "FLUTUAR", nome: "Flutuar" },
  { id: "NENHUMA", nome: "Nenhuma" },
];

export function Personalizacao({
  token,
  detalhe,
  onSaved,
}: {
  token: string;
  detalhe: EventoDetalhe;
  onSaved: () => void;
}) {
  const eventoId = detalhe.id;
  const fileRef = useRef<HTMLInputElement>(null);
  const [enviandoFoto, setEnviandoFoto] = useState(false);
  const [fotoUrl, setFotoUrl] = useState(detalhe.fotoUrl);

  // Aparência (paleta + cor de destaque + fundo)
  const fundoRef = useRef<HTMLInputElement>(null);
  const [paleta, setPaleta] = useState(detalhe.paletaTema ?? "ROSE");
  const [corDestaque, setCorDestaque] = useState(detalhe.corDestaque ?? "");
  const [overlay, setOverlay] = useState(detalhe.fundoOverlay ?? OVERLAY_PADRAO);
  const [animacao, setAnimacao] = useState(detalhe.estiloAnimacao ?? "SUAVE");
  const [fundoUrl, setFundoUrl] = useState(detalhe.fundoUrl);
  const [enviandoFundo, setEnviandoFundo] = useState(false);
  const [salvandoTema, setSalvandoTema] = useState(false);

  const [nome, setNome] = useState(detalhe.nome);
  const [dataEvento, setData] = useState(detalhe.dataEvento ?? "");
  const [local, setLocal] = useState(detalhe.local ?? "");
  const [mensagem, setMensagem] = useState(detalhe.mensagemCasal ?? "");
  const [mensagemFinal, setMensagemFinal] = useState(detalhe.mensagemFinal ?? "");
  const [mensagemAgradecimento, setMensagemAgradecimento] = useState(
    detalhe.mensagemAgradecimento ?? "",
  );
  const [descricao, setDescricao] = useState(detalhe.descricao ?? "");
  const [rsvp, setRsvp] = useState(detalhe.rsvpHabilitado);
  const [dinheiro, setDinheiro] = useState(detalhe.presenteDinheiroHabilitado);
  const [salvando, setSalvando] = useState(false);

  const [previewKey, setPreviewKey] = useState(0);

  // Preview ao vivo: reflete as escolhas (ainda não salvas) via query params no iframe.
  const previewSrc = useMemo(() => {
    const p = new URLSearchParams({
      preview: "1",
      paleta,
      anim: animacao,
      overlay: String(overlay),
    });
    if (corDestaque) p.set("accent", corDestaque);
    return `/e/${detalhe.slug}?${p.toString()}`;
  }, [detalhe.slug, paleta, corDestaque, overlay, animacao]);

  const [previewSrcDebounced, setPreviewSrcDebounced] = useState(previewSrc);
  useEffect(() => {
    const t = setTimeout(() => setPreviewSrcDebounced(previewSrc), 350);
    return () => clearTimeout(t);
  }, [previewSrc]);

  async function enviarFoto(e: React.ChangeEvent<HTMLInputElement>) {
    const arquivo = e.target.files?.[0];
    if (!arquivo) return;
    if (arquivo.size > 8 * 1024 * 1024) {
      toast.error("Imagem muito grande (máx. 8MB).");
      return;
    }
    const fd = new FormData();
    fd.append("file", arquivo);
    setEnviandoFoto(true);
    try {
      const r = await api.upload<{ fotoUrl: string }>(`/events/${eventoId}/photo`, fd, token);
      setFotoUrl(r.fotoUrl);
      toast.success("Foto atualizada!");
      setPreviewKey((k) => k + 1);
      onSaved();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Falha ao enviar a foto");
    } finally {
      setEnviandoFoto(false);
      if (fileRef.current) fileRef.current.value = "";
    }
  }

  async function enviarFundo(e: React.ChangeEvent<HTMLInputElement>) {
    const arquivo = e.target.files?.[0];
    if (!arquivo) return;
    if (arquivo.size > 8 * 1024 * 1024) {
      toast.error("Imagem muito grande (máx. 8MB).");
      return;
    }
    const fd = new FormData();
    fd.append("file", arquivo);
    setEnviandoFundo(true);
    try {
      const r = await api.upload<{ fundoUrl: string }>(`/events/${eventoId}/background`, fd, token);
      setFundoUrl(r.fundoUrl);
      toast.success("Fundo atualizado!");
      setPreviewKey((k) => k + 1);
      onSaved();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Falha ao enviar o fundo");
    } finally {
      setEnviandoFundo(false);
      if (fundoRef.current) fundoRef.current.value = "";
    }
  }

  async function removerFundo() {
    setEnviandoFundo(true);
    try {
      await api.del(`/events/${eventoId}/background`, token);
      setFundoUrl(null);
      toast.success("Fundo removido.");
      setPreviewKey((k) => k + 1);
      onSaved();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Falha ao remover o fundo");
    } finally {
      setEnviandoFundo(false);
    }
  }

  async function salvarTema() {
    setSalvandoTema(true);
    try {
      await api.put(
        `/events/${eventoId}/theme`,
        { paleta, corDestaque: corDestaque || null, overlay, estiloAnimacao: animacao },
        token,
      );
      toast.success("Aparência salva!");
      setPreviewKey((k) => k + 1);
      onSaved();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro ao salvar a aparência");
    } finally {
      setSalvandoTema(false);
    }
  }

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    setSalvando(true);
    try {
      await api.put(
        `/events/${eventoId}`,
        {
          nome,
          descricao: descricao || null,
          dataEvento: dataEvento || null,
          local: local || null,
          mensagemCasal: mensagem || null,
          mensagemFinal: mensagemFinal || null,
          mensagemAgradecimento: mensagemAgradecimento || null,
          rsvpHabilitado: rsvp,
          prazoRsvp: null,
          presenteDinheiroHabilitado: dinheiro,
        },
        token,
      );
      toast.success("Personalização salva!");
      setPreviewKey((k) => k + 1);
      onSaved();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro ao salvar");
    } finally {
      setSalvando(false);
    }
  }

  const foto = mediaUrl(fotoUrl);
  const fundo = mediaUrl(fundoUrl);

  return (
    <div className="grid gap-5 lg:grid-cols-[1fr_360px]">
      <div className="space-y-5">
        {/* Foto do casal */}
        <Card className="rounded-2xl border-border">
          <CardContent className="p-5 sm:p-6">
            <div className="mb-4 flex items-center gap-2 font-medium">
              <Camera className="size-4 text-plum" /> Foto do casal
            </div>
            <div className="flex items-center gap-4">
              <div className="hero-noise relative grid h-28 w-40 flex-none place-items-center overflow-hidden rounded-xl bg-[linear-gradient(135deg,#F0A6C2,#E2A0D6)] text-white/70">
                {foto ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img src={foto} alt="" className="size-full object-cover" />
                ) : (
                  <ImageIcon className="size-7" />
                )}
              </div>
              <div className="space-y-2">
                <p className="text-sm text-muted-foreground">
                  Aparece no topo da página pública. JPG, PNG ou WEBP (máx. 8MB).
                </p>
                <input
                  ref={fileRef}
                  type="file"
                  accept="image/*"
                  capture="environment"
                  className="hidden"
                  onChange={enviarFoto}
                />
                <Button
                  type="button"
                  variant="outline"
                  className="h-10"
                  onClick={() => fileRef.current?.click()}
                  disabled={enviandoFoto}
                >
                  <Camera className="size-4" />
                  {enviandoFoto ? "Enviando..." : foto ? "Trocar foto" : "Enviar foto"}
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Aparência (tema + fundo) */}
        <Card className="rounded-2xl border-border">
          <CardContent className="space-y-5 p-5 sm:p-6">
            <div className="flex items-center gap-2 font-medium">
              <Palette className="size-4 text-plum" /> Aparência do site
            </div>

            <div className="space-y-2">
              <Label>Paleta de cores</Label>
              <div className="flex flex-wrap gap-2">
                {PALETAS_DISPONIVEIS.map((p) => (
                  <button
                    key={p.id}
                    type="button"
                    onClick={() => setPaleta(p.id)}
                    aria-label={p.nome}
                    aria-pressed={paleta === p.id}
                    className={
                      "flex items-center gap-2 rounded-xl border px-3 py-2 text-sm transition-colors " +
                      (paleta === p.id
                        ? "border-plum bg-lilac"
                        : "border-border bg-white hover:bg-lilac/50")
                    }
                  >
                    <span
                      className="size-5 flex-none rounded-full"
                      style={{ background: `linear-gradient(135deg, ${p.rose}, ${p.plum})` }}
                    />
                    {p.nome}
                  </button>
                ))}
              </div>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="p-accent">Cor de destaque (opcional)</Label>
                <div className="flex items-center gap-2">
                  <input
                    id="p-accent"
                    type="color"
                    value={corDestaque || "#c7407a"}
                    onChange={(e) => setCorDestaque(e.target.value)}
                    className="h-11 w-14 flex-none cursor-pointer rounded-lg border border-border bg-white p-1"
                  />
                  <Input
                    className="h-11"
                    placeholder="#RRGGBB (usa a da paleta)"
                    value={corDestaque}
                    onChange={(e) => setCorDestaque(e.target.value)}
                  />
                  {corDestaque && (
                    <Button type="button" variant="outline" className="h-11" onClick={() => setCorDestaque("")}>
                      Limpar
                    </Button>
                  )}
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="p-overlay">Escurecer fundo ({overlay}%)</Label>
                <input
                  id="p-overlay"
                  type="range"
                  min={0}
                  max={100}
                  value={overlay}
                  onChange={(e) => setOverlay(Number(e.target.value))}
                  className="h-11 w-full cursor-pointer accent-plum"
                />
                <p className="text-xs text-muted-foreground">
                  Deixa o texto legível sobre a imagem de fundo.
                </p>
              </div>
            </div>

            <div className="space-y-2">
              <Label>Animação de entrada</Label>
              <div className="flex flex-wrap gap-2">
                {ANIMACOES.map((a) => (
                  <button
                    key={a.id}
                    type="button"
                    onClick={() => setAnimacao(a.id)}
                    aria-pressed={animacao === a.id}
                    className={
                      "rounded-xl border px-3 py-2 text-sm transition-colors " +
                      (animacao === a.id
                        ? "border-plum bg-lilac"
                        : "border-border bg-white hover:bg-lilac/50")
                    }
                  >
                    {a.nome}
                  </button>
                ))}
              </div>
            </div>

            <div className="space-y-2">
              <Label>Imagem de fundo (opcional)</Label>
              <div className="flex items-center gap-4">
                <div className="relative grid h-20 w-32 flex-none place-items-center overflow-hidden rounded-xl border border-border bg-lilac text-muted-foreground">
                  {fundo ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={fundo} alt="" className="size-full object-cover" />
                  ) : (
                    <ImageIcon className="size-6" />
                  )}
                </div>
                <div className="space-y-2">
                  <p className="text-sm text-muted-foreground">
                    Fica atrás de todo o site. JPG, PNG ou WEBP (máx. 8MB).
                  </p>
                  <input
                    ref={fundoRef}
                    type="file"
                    accept="image/*"
                    capture="environment"
                    className="hidden"
                    onChange={enviarFundo}
                  />
                  <div className="flex flex-wrap gap-2">
                    <Button
                      type="button"
                      variant="outline"
                      className="h-10"
                      onClick={() => fundoRef.current?.click()}
                      disabled={enviandoFundo}
                    >
                      <ImageIcon className="size-4" />
                      {enviandoFundo ? "Enviando..." : fundo ? "Trocar fundo" : "Enviar fundo"}
                    </Button>
                    {fundo && (
                      <Button
                        type="button"
                        variant="outline"
                        className="h-10 text-muted-foreground hover:border-rose hover:text-rose"
                        onClick={removerFundo}
                        disabled={enviandoFundo}
                      >
                        <Trash2 className="size-4" /> Remover
                      </Button>
                    )}
                  </div>
                </div>
              </div>
            </div>

            <Button
              type="button"
              className="brand-gradient h-11 w-full border-0 text-white hover:opacity-95"
              onClick={salvarTema}
              disabled={salvandoTema}
            >
              <Save className="size-4" /> {salvandoTema ? "Salvando..." : "Salvar aparência"}
            </Button>
          </CardContent>
        </Card>

        {/* Detalhes do evento */}
        <Card className="rounded-2xl border-border">
          <CardContent className="p-5 sm:p-6">
            <form onSubmit={salvar} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="p-nome">Nome do evento</Label>
                <Input
                  id="p-nome"
                  className="h-11"
                  value={nome}
                  onChange={(e) => setNome(e.target.value)}
                  required
                />
                <p className="text-xs text-muted-foreground">
                  Dica: use &ldquo;Nathan &amp; Thays&rdquo; — o &amp; ganha um destaque elegante.
                </p>
              </div>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="p-data">Data</Label>
                  <Input
                    id="p-data"
                    type="date"
                    className="h-11"
                    value={dataEvento}
                    onChange={(e) => setData(e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="p-local">Local (Onde)</Label>
                  <Input
                    id="p-local"
                    className="h-11"
                    placeholder="Sítio Recanto Verde · Maringá"
                    value={local}
                    onChange={(e) => setLocal(e.target.value)}
                  />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="p-msg">Recado do casal (aparece em destaque)</Label>
                <Textarea
                  id="p-msg"
                  rows={3}
                  maxLength={280}
                  placeholder="Construir um lar ao seu lado já é o nosso maior presente..."
                  value={mensagem}
                  onChange={(e) => setMensagem(e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="p-final">Frase de despedida (rodapé da página)</Label>
                <Input
                  id="p-final"
                  className="h-11"
                  maxLength={120}
                  placeholder="Mal podemos esperar por você."
                  value={mensagemFinal}
                  onChange={(e) => setMensagemFinal(e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="p-thanks">
                  Mensagem de agradecimento (após o convidado confirmar os presentes)
                </Label>
                <Textarea
                  id="p-thanks"
                  rows={2}
                  maxLength={280}
                  placeholder="Muito obrigado por fazer parte do nosso dia! 💕"
                  value={mensagemAgradecimento}
                  onChange={(e) => setMensagemAgradecimento(e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="p-desc">Descrição (usada no compartilhamento)</Label>
                <Textarea
                  id="p-desc"
                  rows={2}
                  value={descricao}
                  onChange={(e) => setDescricao(e.target.value)}
                />
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <Toggle
                  ativo={rsvp}
                  onToggle={() => setRsvp((v) => !v)}
                  titulo="Confirmação de presença"
                  texto="Convidados confirmam presença"
                />
                <Toggle
                  ativo={dinheiro}
                  onToggle={() => setDinheiro((v) => !v)}
                  titulo="Presente em dinheiro"
                  texto="Aceitar valores via Mercado Pago"
                />
              </div>

              <Button
                type="submit"
                className="brand-gradient h-11 w-full border-0 text-white hover:opacity-95"
                disabled={salvando}
              >
                <Save className="size-4" /> {salvando ? "Salvando..." : "Salvar personalização"}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>

      {/* Preview ao vivo */}
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <span className="text-sm font-medium">Pré-visualização</span>
          <div className="flex gap-1">
            <button
              type="button"
              aria-label="Atualizar prévia"
              onClick={() => setPreviewKey((k) => k + 1)}
              className="grid cursor-pointer place-items-center rounded-lg border border-border p-2 text-muted-foreground transition-colors hover:bg-lilac hover:text-plum"
            >
              <RefreshCw className="size-3.5" />
            </button>
            <a
              href={`/e/${detalhe.slug}`}
              target="_blank"
              rel="noreferrer"
              aria-label="Abrir em nova aba"
              className="grid cursor-pointer place-items-center rounded-lg border border-border p-2 text-muted-foreground transition-colors hover:bg-lilac hover:text-plum"
            >
              <ExternalLink className="size-3.5" />
            </a>
          </div>
        </div>
        <div className="overflow-hidden rounded-[26px] border-4 border-white bg-white shadow-[0_30px_60px_-34px_rgba(110,31,67,0.5)]">
          <iframe
            key={previewKey}
            src={previewSrcDebounced}
            title="Pré-visualização da página"
            className="h-[640px] w-full"
          />
        </div>
        <p className="text-center text-xs text-muted-foreground">
          É assim que seus convidados verão a página.
        </p>
      </div>
    </div>
  );
}

function Toggle({
  ativo,
  onToggle,
  titulo,
  texto,
}: {
  ativo: boolean;
  onToggle: () => void;
  titulo: string;
  texto: string;
}) {
  return (
    <button
      type="button"
      onClick={onToggle}
      className={
        "flex cursor-pointer items-center justify-between gap-3 rounded-xl border p-3 text-left transition-colors " +
        (ativo ? "border-lilac-line bg-lilac" : "border-border bg-white")
      }
    >
      <span>
        <span className="block text-sm font-medium">{titulo}</span>
        <span className="block text-xs text-muted-foreground">{texto}</span>
      </span>
      <span
        className={
          "relative h-6 w-10 flex-none rounded-full transition-colors " +
          (ativo ? "brand-gradient" : "bg-muted-foreground/30")
        }
      >
        <span
          className={
            "absolute top-0.5 size-5 rounded-full bg-white shadow transition-all " +
            (ativo ? "left-[18px]" : "left-0.5")
          }
        />
      </span>
    </button>
  );
}
