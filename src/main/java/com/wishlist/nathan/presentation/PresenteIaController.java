package com.wishlist.nathan.presentation;

import com.wishlist.nathan.application.dto.ArquivoUpload;
import com.wishlist.nathan.application.dto.ItemPresenteExtraido;
import com.wishlist.nathan.application.dto.SessaoAdmin;
import com.wishlist.nathan.application.usecase.ConfirmarImportacaoIa;
import com.wishlist.nathan.application.usecase.ExtrairListaPresentesIa;
import com.wishlist.nathan.infrastructure.ai.ClienteClaude;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Importação de lista de presentes por IA. Fluxo em duas etapas: extrair (preview,
 * não grava) → confirmar (materializa). Mantido separado do {@link EventoController}
 * para não inchar o CRUD do evento.
 */
@RestController
@RequestMapping("/events/{eventoId}/gifts")
public class PresenteIaController {

    private final ExtrairListaPresentesIa extrair;
    private final ConfirmarImportacaoIa confirmar;

    public PresenteIaController(ExtrairListaPresentesIa extrair, ConfirmarImportacaoIa confirmar) {
        this.extrair = extrair;
        this.confirmar = confirmar;
    }

    /** Extrai de uma imagem/print ou PDF/planilha (preview, sem gravar). */
    @PostMapping("/ai-extract")
    public List<ItemPresenteExtraido> extrairDeArquivo(@AuthenticationPrincipal SessaoAdmin sessao,
                                                       @PathVariable UUID eventoId,
                                                       @RequestParam("file") MultipartFile arquivo,
                                                       @RequestParam(defaultValue = "true") boolean estimar) {
        ArquivoUpload up = new ArquivoUpload(arquivo.getOriginalFilename(), arquivo.getContentType(), bytes(arquivo));
        return extrair.deArquivo(sessao.contaId(), eventoId, up, estimar);
    }

    /** Extrai de texto colado ou de um link de outro site (preview, sem gravar). */
    @PostMapping("/ai-extract-text")
    public List<ItemPresenteExtraido> extrairDeTexto(@AuthenticationPrincipal SessaoAdmin sessao,
                                                     @PathVariable UUID eventoId,
                                                     @RequestBody ExtrairTextoRequest req) {
        boolean estimar = req.estimar() == null || req.estimar();
        if (req.link() != null && !req.link().isBlank()) {
            return extrair.deLink(sessao.contaId(), eventoId, req.link(), estimar);
        }
        return extrair.deTexto(sessao.contaId(), eventoId, req.texto(), estimar);
    }

    /** Materializa os itens revisados pelo casal. */
    @PostMapping("/ai-import")
    public ConfirmarImportacaoIa.Resultado importar(@AuthenticationPrincipal SessaoAdmin sessao,
                                                    @PathVariable UUID eventoId,
                                                    @RequestBody ImportarIaRequest req) {
        List<ItemPresenteExtraido> itens = req.itens() == null ? List.of() : req.itens().stream()
                .map(i -> new ItemPresenteExtraido(i.nome(), i.variante(), i.quantidade(),
                        i.linkProduto(), i.precoEstimado()))
                .toList();
        return confirmar.executar(sessao.contaId(), eventoId, itens);
    }

    @ExceptionHandler(ClienteClaude.IaIndisponivelException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ProblemDetail iaIndisponivel(ClienteClaude.IaIndisponivelException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    private static byte[] bytes(MultipartFile arquivo) {
        try {
            return arquivo.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Falha ao ler o arquivo enviado");
        }
    }

    public record ExtrairTextoRequest(String texto, String link, Boolean estimar) {}

    public record ImportarIaRequest(List<ItemDto> itens) {}

    public record ItemDto(String nome, String variante, Integer quantidade, String linkProduto,
                          BigDecimal precoEstimado) {}
}
