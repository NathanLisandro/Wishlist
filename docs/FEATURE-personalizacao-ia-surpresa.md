# Épico: Personalização do site + IA + Modo surpresa

Documentação do conjunto de features entregues sobre a fundação existente (Spring Boot 4 / Clean Architecture no backend, Next.js 16 no frontend). Cobre 7 fases independentes, todas verificadas por compilação (backend + testes) e type-check (frontend).

> Convenções mantidas: domínio puro em `domain/`, casos de uso + ports em `application/`, adapters em `infrastructure/`, controllers em `presentation/`. Toda integração externa entra **atrás de um Port**. Autorização centralizada em `AcessoEvento`. Migrations Flyway `V<n>__descricao.sql`.

---

## Índice das migrations

| Versão | Arquivo | O que adiciona em `event`/`gift_item` |
|--------|---------|----------------------------------------|
| V7 | `V7__event_theme.sql` | `theme_palette`, `accent_color`, `background_url`, `background_overlay` |
| V8 | `V8__event_animation.sql` | `animation_style` |
| V9 | `V9__gift_price.sql` | `gift_item.estimated_price numeric(12,2)` |
| V10 | `V10__event_surprise_mode.sql` | `surprise_mode boolean not null default false` |

As migrations rodam no startup do backend. **Após puxar este código, reinicie o backend** (não é hot-reload se rodando via jar).

---

## Fase 1 — Tema + fundo + preview ao vivo

O casal personaliza a aparência do site público: paleta pré-definida, cor de destaque, imagem de fundo com overlay.

**Domínio**
- `domain/evento/PaletaTema.java` — whitelist de paletas (`ROSE`, `AZUL`, `VERDE`, `DOURADO`, `GRAFITE`). Valor inválido cai no padrão `ROSE`.
- `domain/evento/Tema.java` — **value object** (paleta + cor de destaque + overlay + animação) com validação coesa (hex `#RRGGBB`, overlay 0–100).
- `Evento`: campos `paletaTema`, `corDestaque`, `fundoUrl`, `fundoOverlay`; métodos `personalizarTema(Tema)`, `definirFundo(String)`, `hidratarTema(...)`.

**Aplicação / Infra**
- `PersonalizarTema` (use case) — monta o `Tema` e aplica (dono ou co-gestor).
- `SalvarFundoEvento` (use case) — upload/troca/**remoção** do fundo, reusando `ArmazenamentoPort` (mesmo padrão de `SalvarFotoEvento`). Apaga o arquivo anterior.
- `application/dto/ArquivoUpload` — DTO de arquivo (desacopla `MultipartFile` da camada de aplicação).

**Endpoints** (`EventoController`)
- `PUT /events/{id}/theme` — `{ paleta, corDestaque, overlay, estiloAnimacao }`.
- `POST /events/{id}/background` (multipart `file`) → `{ fundoUrl }`.
- `DELETE /events/{id}/background` — remove o fundo.
- Campos de tema expostos em `ConsultarEventoPublico` (SSR) e `ConsultarEventoAdmin`.

**Frontend**
- `lib/temas.ts` — mapa `paleta → CSS variables` (`--rose`, `--plum`, `--lilac-tint`, `--accent`, `--primary`, `--ring`, etc.). Espelha o whitelist do backend.
- `app/e/[slug]/page.tsx` (SSR) — aplica as CSS vars **inline no `<main>`** (sobrepõe o `:root` de `globals.css` sem flash) + `<div>` de fundo com overlay (`rgba(0,0,0, overlay/100)`).
- `admin/.../Personalizacao.tsx` — card "Aparência": swatches de paleta, color picker do accent, slider de overlay, upload/remover fundo.

**Preview ao vivo (importante)**
O preview é um iframe da página pública. Para refletir escolhas **ainda não salvas**, a página pública aceita um **modo preview por query params**:

```
/e/{slug}?preview=1&paleta=GRAFITE&accent=%23ff006f&overlay=80&anim=flutuar
```

Em modo preview a página usa os params em vez dos valores salvos. O `Personalizacao.tsx` monta esse `src` a partir do formulário (com debounce de 350ms) → o iframe atualiza enquanto o casal mexe. Sem isso, o preview só mudaria após salvar.

---

## Fase 2 — Animações de entrada

- `domain/evento/EstiloAnimacao.java` — whitelist `NENHUMA`, `SUAVE` (atual `.rise`), `CASCATA`, `FLUTUAR`.
- Persistido junto do tema (campo `estiloAnimacao` no `PersonalizarTema.Comando` e no `Tema`).
- Frontend: classes CSS em `app/globals.css` selecionadas por `[data-anim="..."]` no `<main>`, todas sob `@media (prefers-reduced-motion: no-preference)`. Seletor de animação no card de aparência.

---

## Fase 3 — Câmera + imagem de presente

- **Câmera**: atributo `capture="environment"` nos `<input type="file">` (foto do casal, fundo, imagem de presente) → abre a câmera no celular.
- **Imagem de presente** (antes só URL): `ItemPresente.definirImagem(...)`, use case `SalvarImagemPresente`, endpoint `POST /events/{id}/gifts/{presenteId}/image`. No `GestaoPresentes.tsx`, upload no modal de presente (mantendo a opção de URL). Só apaga a imagem anterior se era upload nosso (não apaga URL externa).

---

## Fase 4 — Paginação no admin

- `application/dto/Pagina<T>` — DTO `{ itens, total, page, size }` com `mapear(fn)`; confina `Pageable`/`Page` (Spring) aos adapters.
- Ports/adapters/repositórios de presente e convidado ganham métodos paginados (`listarDoEventoPaginado`, `listarPorEventoPaginado`).
- Use cases `ListarPresentesAdmin.executarPaginado` e `ListarConvidados.executarPaginado`.
- Endpoints `GET /events/{id}/gifts` e `GET /events/{id}/guests` agora aceitam `?page=&size=` e retornam `Pagina<T>`.
- Frontend: componente `components/Paginacao.tsx` (anterior/próxima) em `GestaoPresentes`/`GestaoConvidados`.
- **A lista pública de presentes (`PassoPresente.tsx`) NÃO pagina** — convidados (inclusive idosos) veem tudo num scroll só. Decisão firme.

---

## Fase 5 — Preço estimado + ticket médio (só o dono)

- `gift_item.estimated_price` (V9). `ItemPresente.precoEstimado` usa o VO `domain/comum/Dinheiro` (BRL). O preço é preservado ao editar o presente.
- `ConsultarTicketMedio` — média dos preços estimados. **Checagem estrita de dono** (`evento.contaId == contaId`), não passa por `AcessoEvento` (co-gestor não vê). Em modo surpresa, nem o dono vê (ver Fase 7).
- `GET /events/{id}/average-ticket` → `{ ticketMedio, itensComPreco }`. **Nunca exposto no DTO público.**
- Frontend: card de ticket médio no topo da aba Presentes, renderizado só quando `dono`.

O preço é preenchido pela importação por IA (Fase 6); não há campo manual de preço.

---

## Fase 6 — Importação por IA (Claude)

Lê uma lista de presentes **já existente** do casal (foto/print, PDF/planilha, texto colado ou link de outro site), extrai os itens, estima preços e materializa — com **preview antes de confirmar**.

**Dependência**: `com.anthropic:anthropic-java:2.34.0` (`build.gradle`). Modelo padrão `claude-opus-4-8`.

**Config** (`AppProperties.Ai`, prefixo `app.ai.*`, em `application.yml`):
```yaml
app:
  ai:
    api-key: ${ANTHROPIC_API_KEY:}     # sem chave, os endpoints de IA retornam 503 amigável
    model:   ${ANTHROPIC_MODEL:claude-opus-4-8}
    habilitado: ${AI_ENABLED:true}
```

**Ports** (provider 100% isolado, em `application/port/`)
- `ExtratorListaIaPort` — `EntradaIa (IMAGEM|PDF|TEXTO)` → `List<ItemPresenteExtraido>` (sem preço).
- `EstimadorPrecoIaPort` — `List<nome>` → `Map<nome, BigDecimal BRL>`.
- `BuscadorHtmlPort` — URL → texto (para o caso de link).

**Adapters** (`infrastructure/ai/`)
- `ClienteClaude` — fábrica/cache do `AnthropicClient`; lança erro amigável (`IaIndisponivelException` → 503) se a chave não estiver configurada.
- `ClaudeExtratorListaAdapter` — visão + PDF nativos; **structured outputs** (`outputConfig(ListaExtraida.class)` + beta header `structured-outputs-2025-11-13`) — sem parsing frágil; system prompt **cacheado**.
- `ClaudeEstimadorPrecoAdapter` — structured outputs; estima preço de varejo BR pelo conhecimento do modelo. Uma chamada para a lista toda.
- `BuscadorHtmlHttp` — `java.net.http.HttpClient`. **Proteção SSRF**: só http/https, bloqueia IP privado/loopback/link-local/metadata, não segue redirecionamentos, limita tempo e tamanho. Faz strip de HTML → texto.

**Use cases**
- `ExtrairListaPresentesIa` — `deArquivo` / `deTexto` / `deLink`; autoriza (gestão de presentes), extrai e (opcional) estima preço. **Não grava** — devolve preview.
- `ConfirmarImportacaoIa` — materializa a lista revisada **reusando `AdicionarPresente`** (mesma idempotência por `natural_key`, transação por item), grava o preço estimado e registra um `ImportJob` (`TipoImportacao.GIFTS`). Devolve `{ importJobId, total, ok, falhas }`.

**Endpoints** (`PresenteIaController` — separado do `EventoController` para não inchar o CRUD)
- `POST /events/{id}/gifts/ai-extract` (multipart: imagem/PDF) → preview.
- `POST /events/{id}/gifts/ai-extract-text` (JSON `{ texto | link, estimar }`) → preview.
- `POST /events/{id}/gifts/ai-import` (JSON `{ itens: [...] }`) → materializa.

**Frontend**: `ImportarComIa.tsx` — botão "Importar com IA" no `GestaoPresentes`; modal com 3 origens (foto/PDF, texto, link) + opção de estimar preço; **preview editável** (remover itens) antes de confirmar.

---

## Fase 7 — Modo surpresa

Quando ligado, **só o co-gestor (terceiro) mexe nos presentes** — o dono não vê nem edita a lista, para ser surpreendido. Construído sobre a co-gestão existente (`event_collaborator`).

- `event.surprise_mode` (V10). `Evento.modoSurpresa` + `definirModoSurpresa`.
- `AcessoEvento.exigirGestaoPresentes(contaId, eventoId)` — igual a `exigir` (dono ou co-gestor), **exceto** que bloqueia o **dono** (404) quando o modo surpresa está ligado. Co-gestor passa nos dois modos.
- Todos os use cases de presente passaram a usar `exigirGestaoPresentes`: `AdicionarPresente`, `EditarPresente`, `RemoverPresente`, `RemoverTodosPresentes`, `ListarPresentesAdmin`, `SalvarImagemPresente`, `ExtrairListaPresentesIa`, `ConfirmarImportacaoIa`.
- `ConsultarTicketMedio` — em modo surpresa nem o dono vê (filtro `dono && !modoSurpresa`).
- `AlternarModoSurpresa` (só o dono) + `PUT /events/{id}/surprise` `{ ativo }`.
- `ConsultarEventoAdmin` expõe `modoSurpresa` e `podeGerenciarPresentes` (`= !(modoSurpresa && dono)`).
- Frontend: toggle no modal de co-gestão (`CoGestao.tsx`, só dono); a aba **"Presentes" some** para o dono quando o modo está ligado (`page.tsx`).

---

## Configuração necessária

| Variável | Para quê | Padrão |
|----------|----------|--------|
| `ANTHROPIC_API_KEY` | Importação por IA (Fase 6). Sem ela, endpoints de IA → 503 amigável; resto funciona. | vazio |
| `ANTHROPIC_MODEL` | Modelo da Anthropic | `claude-opus-4-8` |
| `AI_ENABLED` | Liga/desliga a feature de IA | `true` |

Limite de upload (foto/fundo/imagem/PDF): 8MB (`spring.servlet.multipart`).

---

## Verificação manual (end-to-end)

1. **Migrations**: subir o backend e confirmar V7–V10 aplicadas.
2. **Tema**: trocar paleta/accent/overlay/animação → preview no iframe muda **ao vivo**; salvar; abrir `/e/{slug}` e conferir o SSR (CSS vars inline no HTML, sem flash).
3. **Fundo**: enviar imagem → aparece atrás do site; **Remover** → volta ao gradiente; overlay garante legibilidade.
4. **Câmera/imagem de presente**: no celular, o botão de foto abre a câmera; upload reflete no card do presente.
5. **Paginação**: criar >20 presentes/convidados e navegar; lista pública continua mostrando tudo.
6. **Ticket médio**: como dono, ver o card; como co-gestor, confirmar que NÃO aparece.
7. **IA** (requer `ANTHROPIC_API_KEY`): testar os 4 formatos (foto, texto, PDF/planilha, link) → preview → confirmar → presentes criados (reimportar o mesmo conteúdo não duplica). Preço estimado preenchido.
8. **Modo surpresa**: ligar como dono → aba Presentes some para o dono e endpoints de presente retornam 404 para ele; co-gestor continua gerenciando.

---

## Decisões de design (resumo)

- **DTOs/VOs** preferidos a passar muitos primitivos: `Tema`, `ArquivoUpload`, `Pagina<T>`, `ItemPresenteExtraido`, `EntradaIa`.
- IA atrás de **ports** — trocar de provider não toca a aplicação.
- IA materializa **reusando o pipeline de importação** existente (idempotência + `ImportJob`), em vez de recriar lógica.
- Controller dedicado para IA (`PresenteIaController`) para não inchar o `EventoController`.
- Preço/ticket são do **dono**; nunca expostos no público.
- Lista pública **sem paginação** (acessibilidade).
