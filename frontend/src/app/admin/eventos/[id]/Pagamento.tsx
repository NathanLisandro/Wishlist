"use client";

import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Copy, Wallet } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";

export function Pagamento({ token }: { token: string }) {
  const [accessToken, setAccessToken] = useState("");
  const [publicKey, setPublicKey] = useState("");
  const [webhookSecret, setWebhookSecret] = useState("");
  const [configurado, setConfigurado] = useState<boolean | null>(null);
  const [salvando, setSalvando] = useState(false);
  const [accountId, setAccountId] = useState("");
  const [baseUrl, setBaseUrl] = useState("");

  useEffect(() => {
    api
      .get<{ configurado: boolean; publicKey?: string }>("/account/payment-config", token)
      .then((r) => {
        setConfigurado(r.configurado);
        if (r.publicKey) setPublicKey(r.publicKey);
      })
      .catch(() => setConfigurado(false));
    api
      .get<{ id: string }>("/account/me", token)
      .then((r) => setAccountId(r.id))
      .catch(() => {});
  }, [token]);

  const webhookUrl = accountId
    ? `${(baseUrl || "https://SEU-DOMINIO-PUBLICO").replace(/\/$/, "")}/webhooks/mercadopago?accountId=${accountId}`
    : "";

  function copiar() {
    navigator.clipboard.writeText(webhookUrl);
    toast.success("URL copiada!");
  }

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    setSalvando(true);
    try {
      await api.put(
        "/account/payment-config",
        { accessToken, publicKey, webhookSecret: webhookSecret || null },
        token,
      );
      toast.success("Mercado Pago configurado!");
      setConfigurado(true);
      setAccessToken("");
      setWebhookSecret("");
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro ao salvar");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <Card className="max-w-xl rounded-2xl border-border">
      <CardContent className="p-5 sm:p-6">
        <div className="mb-4 flex items-center gap-2 font-medium">
          <span className="grid size-9 place-items-center rounded-lg bg-emerald-50 text-emerald-600">
            <Wallet className="size-4" />
          </span>
          Mercado Pago {configurado ? "· configurado" : ""}
        </div>
        <form onSubmit={salvar} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="at">Access Token</Label>
            <Input
              id="at"
              className="h-11"
              value={accessToken}
              placeholder="APP_USR-... ou TEST-..."
              onChange={(e) => setAccessToken(e.target.value)}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="pk">Public Key</Label>
            <Input id="pk" className="h-11" value={publicKey} onChange={(e) => setPublicKey(e.target.value)} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="ws">Webhook Secret (assinatura)</Label>
            <Input
              id="ws"
              className="h-11"
              value={webhookSecret}
              onChange={(e) => setWebhookSecret(e.target.value)}
            />
          </div>
          <p className="text-xs text-muted-foreground">
            As credenciais ficam cifradas no servidor. Valem para todos os eventos da conta.
          </p>
          <Button
            type="submit"
            className="brand-gradient h-11 border-0 text-white hover:opacity-95"
            disabled={salvando}
          >
            {salvando ? "Salvando..." : "Salvar"}
          </Button>
        </form>

        <div className="mt-6 space-y-2 rounded-xl border border-border bg-muted/40 p-4">
          <p className="text-sm font-medium">URL do webhook (cole no Mercado Pago)</p>
          <div className="space-y-1">
            <Label htmlFor="base" className="text-xs">
              Seu domínio público (ngrok ou prod)
            </Label>
            <Input
              id="base"
              className="h-10"
              placeholder="https://abc123.ngrok-free.app"
              value={baseUrl}
              onChange={(e) => setBaseUrl(e.target.value)}
            />
          </div>
          <div className="flex gap-2">
            <Input readOnly value={webhookUrl} className="h-10 font-mono text-xs" />
            <Button type="button" variant="outline" className="h-10" onClick={copiar} disabled={!webhookUrl}>
              <Copy className="size-3.5" /> Copiar
            </Button>
          </div>
          <p className="text-xs text-muted-foreground">
            No painel do Mercado Pago (Suas integrações → Webhooks), cole essa URL, marque o evento{" "}
            <strong>Pagamentos</strong> e copie a assinatura secreta para o campo acima.
          </p>
        </div>
      </CardContent>
    </Card>
  );
}
