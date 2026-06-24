package com.wishlist.nathan.presentation;

import com.wishlist.nathan.application.dto.ArquivoUpload;
import com.wishlist.nathan.application.dto.Pagina;
import com.wishlist.nathan.application.dto.SessaoAdmin;
import com.wishlist.nathan.application.usecase.AdicionarConvidado;
import com.wishlist.nathan.application.usecase.AdicionarPresente;
import com.wishlist.nathan.application.usecase.AlternarModoSurpresa;
import com.wishlist.nathan.application.usecase.AtualizarEvento;
import com.wishlist.nathan.application.usecase.ConsultarEventoAdmin;
import com.wishlist.nathan.application.usecase.ConsultarTicketMedio;
import com.wishlist.nathan.application.usecase.CriarEvento;
import com.wishlist.nathan.application.usecase.GerarConviteGestao;
import com.wishlist.nathan.application.usecase.ListarColaboradores;
import com.wishlist.nathan.application.usecase.RemoverColaborador;
import com.wishlist.nathan.application.usecase.EditarConvidado;
import com.wishlist.nathan.application.usecase.EditarPresente;
import com.wishlist.nathan.application.usecase.ImportarConvidados;
import com.wishlist.nathan.application.usecase.ImportarListaPresentes;
import com.wishlist.nathan.application.usecase.ListarConvidados;
import com.wishlist.nathan.application.usecase.ListarEventos;
import com.wishlist.nathan.application.usecase.ListarPresentesAdmin;
import com.wishlist.nathan.application.usecase.RemoverConvidado;
import com.wishlist.nathan.application.usecase.RemoverPresente;
import com.wishlist.nathan.application.usecase.PersonalizarTema;
import com.wishlist.nathan.application.usecase.RemoverTodosConvidados;
import com.wishlist.nathan.application.usecase.RemoverTodosPresentes;
import com.wishlist.nathan.application.usecase.SalvarFotoEvento;
import com.wishlist.nathan.application.usecase.SalvarFundoEvento;
import com.wishlist.nathan.application.usecase.SalvarImagemPresente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/events")
public class EventoController {

    private final CriarEvento criarEvento;
    private final ConsultarEventoAdmin consultarEventoAdmin;
    private final AtualizarEvento atualizarEvento;
    private final SalvarFotoEvento salvarFotoEvento;
    private final SalvarFundoEvento salvarFundoEvento;
    private final PersonalizarTema personalizarTema;
    private final AdicionarPresente adicionarPresente;
    private final EditarPresente editarPresente;
    private final RemoverPresente removerPresente;
    private final RemoverTodosPresentes removerTodosPresentes;
    private final ListarPresentesAdmin listarPresentesAdmin;
    private final SalvarImagemPresente salvarImagemPresente;
    private final ConsultarTicketMedio consultarTicketMedio;
    private final AlternarModoSurpresa alternarModoSurpresa;
    private final AdicionarConvidado adicionarConvidado;
    private final EditarConvidado editarConvidado;
    private final RemoverConvidado removerConvidado;
    private final RemoverTodosConvidados removerTodosConvidados;
    private final ListarConvidados listarConvidados;
    private final ImportarListaPresentes importarPresentes;
    private final ImportarConvidados importarConvidados;
    private final ListarEventos listarEventos;
    private final GerarConviteGestao gerarConviteGestao;
    private final ListarColaboradores listarColaboradores;
    private final RemoverColaborador removerColaborador;

    public EventoController(CriarEvento criarEvento, ConsultarEventoAdmin consultarEventoAdmin,
                           AtualizarEvento atualizarEvento, SalvarFotoEvento salvarFotoEvento,
                           SalvarFundoEvento salvarFundoEvento, PersonalizarTema personalizarTema,
                           AdicionarPresente adicionarPresente, EditarPresente editarPresente,
                           RemoverPresente removerPresente, RemoverTodosPresentes removerTodosPresentes,
                           ListarPresentesAdmin listarPresentesAdmin, SalvarImagemPresente salvarImagemPresente,
                           ConsultarTicketMedio consultarTicketMedio, AlternarModoSurpresa alternarModoSurpresa,
                           AdicionarConvidado adicionarConvidado, EditarConvidado editarConvidado,
                           RemoverConvidado removerConvidado, RemoverTodosConvidados removerTodosConvidados,
                           ListarConvidados listarConvidados, ImportarListaPresentes importarPresentes,
                           ImportarConvidados importarConvidados, ListarEventos listarEventos,
                           GerarConviteGestao gerarConviteGestao, ListarColaboradores listarColaboradores,
                           RemoverColaborador removerColaborador) {
        this.criarEvento = criarEvento;
        this.consultarEventoAdmin = consultarEventoAdmin;
        this.atualizarEvento = atualizarEvento;
        this.salvarFotoEvento = salvarFotoEvento;
        this.salvarFundoEvento = salvarFundoEvento;
        this.personalizarTema = personalizarTema;
        this.adicionarPresente = adicionarPresente;
        this.editarPresente = editarPresente;
        this.removerPresente = removerPresente;
        this.removerTodosPresentes = removerTodosPresentes;
        this.listarPresentesAdmin = listarPresentesAdmin;
        this.salvarImagemPresente = salvarImagemPresente;
        this.consultarTicketMedio = consultarTicketMedio;
        this.alternarModoSurpresa = alternarModoSurpresa;
        this.adicionarConvidado = adicionarConvidado;
        this.editarConvidado = editarConvidado;
        this.removerConvidado = removerConvidado;
        this.removerTodosConvidados = removerTodosConvidados;
        this.listarConvidados = listarConvidados;
        this.importarPresentes = importarPresentes;
        this.importarConvidados = importarConvidados;
        this.listarEventos = listarEventos;
        this.gerarConviteGestao = gerarConviteGestao;
        this.listarColaboradores = listarColaboradores;
        this.removerColaborador = removerColaborador;
    }

    @GetMapping
    public List<ListarEventos.EventoResumo> listar(@AuthenticationPrincipal SessaoAdmin sessao) {
        return listarEventos.executar(sessao.contaId());
    }

    @PostMapping
    public ResponseEntity<Map<String, UUID>> criar(@AuthenticationPrincipal SessaoAdmin sessao,
                                                   @Valid @RequestBody EventoRequest req) {
        UUID id = criarEvento.executar(sessao.contaId(), new CriarEvento.Comando(
                req.categoriaId(), req.slug(), req.nome(), req.descricao(), req.dataEvento(),
                req.rsvpHabilitado(), req.prazoRsvp(), req.presenteDinheiroHabilitado()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @GetMapping("/{eventoId}")
    public ConsultarEventoAdmin.Resultado detalhe(@AuthenticationPrincipal SessaoAdmin sessao,
                                                  @PathVariable UUID eventoId) {
        return consultarEventoAdmin.executar(sessao.contaId(), eventoId);
    }

    @PutMapping("/{eventoId}")
    public ResponseEntity<Void> atualizar(@AuthenticationPrincipal SessaoAdmin sessao,
                                          @PathVariable UUID eventoId,
                                          @Valid @RequestBody AtualizarEventoRequest req) {
        atualizarEvento.executar(sessao.contaId(), eventoId, new AtualizarEvento.Comando(
                req.nome(), req.descricao(), req.dataEvento(), req.local(), req.mensagemCasal(),
                req.mensagemFinal(), req.mensagemAgradecimento(), req.rsvpHabilitado(), req.prazoRsvp(),
                req.presenteDinheiroHabilitado()));
        return ResponseEntity.noContent().build();
    }

    // ---- Co-gestão (compartilhar o evento) ----

    @PostMapping("/{eventoId}/manage-invite")
    public Map<String, String> gerarConvite(@AuthenticationPrincipal SessaoAdmin sessao,
                                            @PathVariable UUID eventoId) {
        return Map.of("token", gerarConviteGestao.executar(sessao.contaId(), eventoId));
    }

    @DeleteMapping("/{eventoId}/manage-invite")
    public ResponseEntity<Void> revogarConvite(@AuthenticationPrincipal SessaoAdmin sessao,
                                               @PathVariable UUID eventoId) {
        gerarConviteGestao.revogar(sessao.contaId(), eventoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventoId}/collaborators")
    public List<ListarColaboradores.Colaborador> colaboradores(@AuthenticationPrincipal SessaoAdmin sessao,
                                                               @PathVariable UUID eventoId) {
        return listarColaboradores.executar(sessao.contaId(), eventoId);
    }

    @DeleteMapping("/{eventoId}/collaborators/{colaboradorId}")
    public ResponseEntity<Void> removerColaborador(@AuthenticationPrincipal SessaoAdmin sessao,
                                                   @PathVariable UUID eventoId,
                                                   @PathVariable UUID colaboradorId) {
        removerColaborador.executar(sessao.contaId(), eventoId, colaboradorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventoId}/photo")
    public Map<String, String> enviarFoto(@AuthenticationPrincipal SessaoAdmin sessao,
                                          @PathVariable UUID eventoId,
                                          @RequestParam("file") MultipartFile arquivo) {
        String url = salvarFotoEvento.executar(sessao.contaId(), eventoId,
                arquivo.getOriginalFilename(), arquivo.getContentType(), bytes(arquivo));
        return Map.of("fotoUrl", url);
    }

    // ---- Personalização visual (tema + fundo) ----

    @PutMapping("/{eventoId}/theme")
    public ResponseEntity<Void> personalizarTema(@AuthenticationPrincipal SessaoAdmin sessao,
                                                 @PathVariable UUID eventoId,
                                                 @RequestBody TemaRequest req) {
        personalizarTema.executar(sessao.contaId(), eventoId,
                new PersonalizarTema.Comando(req.paleta(), req.corDestaque(), req.overlay(), req.estiloAnimacao()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventoId}/background")
    public Map<String, String> enviarFundo(@AuthenticationPrincipal SessaoAdmin sessao,
                                           @PathVariable UUID eventoId,
                                           @RequestParam("file") MultipartFile arquivo) {
        String url = salvarFundoEvento.executar(sessao.contaId(), eventoId, arquivoUpload(arquivo));
        return Map.of("fundoUrl", url);
    }

    @DeleteMapping("/{eventoId}/background")
    public ResponseEntity<Void> removerFundo(@AuthenticationPrincipal SessaoAdmin sessao,
                                             @PathVariable UUID eventoId) {
        salvarFundoEvento.remover(sessao.contaId(), eventoId);
        return ResponseEntity.noContent().build();
    }

    /** Liga/desliga o modo surpresa (só o dono). */
    @PutMapping("/{eventoId}/surprise")
    public ResponseEntity<Void> alternarSurpresa(@AuthenticationPrincipal SessaoAdmin sessao,
                                                 @PathVariable UUID eventoId,
                                                 @RequestBody SurpresaRequest req) {
        alternarModoSurpresa.executar(sessao.contaId(), eventoId, req.ativo());
        return ResponseEntity.noContent().build();
    }

    // ---- Presentes (gestão) ----

    @GetMapping("/{eventoId}/gifts")
    public Pagina<ListarPresentesAdmin.ItemAdmin> listarPresentes(@AuthenticationPrincipal SessaoAdmin sessao,
                                                                  @PathVariable UUID eventoId,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size) {
        return listarPresentesAdmin.executarPaginado(sessao.contaId(), eventoId, page, size);
    }

    @PostMapping("/{eventoId}/gifts")
    public ResponseEntity<Map<String, UUID>> adicionarPresente(@AuthenticationPrincipal SessaoAdmin sessao,
                                                               @PathVariable UUID eventoId,
                                                               @Valid @RequestBody PresenteRequest req) {
        UUID id = adicionarPresente.executar(sessao.contaId(), eventoId, new AdicionarPresente.Comando(
                req.nome(), req.variante(), req.quantidade(), req.linkProduto(), req.imagemUrl()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @PutMapping("/{eventoId}/gifts/{presenteId}")
    public ResponseEntity<Void> editarPresente(@AuthenticationPrincipal SessaoAdmin sessao,
                                               @PathVariable UUID eventoId, @PathVariable UUID presenteId,
                                               @Valid @RequestBody PresenteRequest req) {
        editarPresente.executar(sessao.contaId(), eventoId, presenteId, new EditarPresente.Comando(
                req.nome(), req.variante(), req.quantidade(), req.linkProduto(), req.imagemUrl()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{eventoId}/gifts/{presenteId}")
    public ResponseEntity<Void> removerPresente(@AuthenticationPrincipal SessaoAdmin sessao,
                                                @PathVariable UUID eventoId, @PathVariable UUID presenteId) {
        removerPresente.executar(sessao.contaId(), eventoId, presenteId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{eventoId}/gifts")
    public Map<String, Long> removerTodosPresentes(@AuthenticationPrincipal SessaoAdmin sessao,
                                                   @PathVariable UUID eventoId) {
        long removidos = removerTodosPresentes.executar(sessao.contaId(), eventoId);
        return Map.of("removidos", removidos);
    }

    @PostMapping("/{eventoId}/gifts/{presenteId}/image")
    public Map<String, String> enviarImagemPresente(@AuthenticationPrincipal SessaoAdmin sessao,
                                                    @PathVariable UUID eventoId, @PathVariable UUID presenteId,
                                                    @RequestParam("file") MultipartFile arquivo) {
        String url = salvarImagemPresente.executar(sessao.contaId(), eventoId, presenteId, arquivoUpload(arquivo));
        return Map.of("imagemUrl", url);
    }

    /** Ticket médio dos presentes (média dos preços estimados) — só o dono. */
    @GetMapping("/{eventoId}/average-ticket")
    public ConsultarTicketMedio.Resultado ticketMedio(@AuthenticationPrincipal SessaoAdmin sessao,
                                                      @PathVariable UUID eventoId) {
        return consultarTicketMedio.executar(sessao.contaId(), eventoId);
    }

    // ---- Convidados ----

    @GetMapping("/{eventoId}/guests")
    public Pagina<ListarConvidados.ConvidadoPainel> listarConvidados(@AuthenticationPrincipal SessaoAdmin sessao,
                                                                     @PathVariable UUID eventoId,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        return listarConvidados.executarPaginado(sessao.contaId(), eventoId, page, size);
    }

    @PostMapping("/{eventoId}/guests")
    public ResponseEntity<Map<String, UUID>> adicionarConvidado(@AuthenticationPrincipal SessaoAdmin sessao,
                                                                @PathVariable UUID eventoId,
                                                                @Valid @RequestBody ConvidadoRequest req) {
        UUID id = adicionarConvidado.executar(sessao.contaId(), eventoId, new AdicionarConvidado.Comando(
                req.nomeCompleto(), req.telefone(), req.acompanhantesPermitidos()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @PutMapping("/{eventoId}/guests/{convidadoId}")
    public ResponseEntity<Void> editarConvidado(@AuthenticationPrincipal SessaoAdmin sessao,
                                                @PathVariable UUID eventoId, @PathVariable UUID convidadoId,
                                                @Valid @RequestBody ConvidadoRequest req) {
        editarConvidado.executar(sessao.contaId(), eventoId, convidadoId, new EditarConvidado.Comando(
                req.nomeCompleto(), req.telefone(), req.acompanhantesPermitidos()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{eventoId}/guests/{convidadoId}")
    public ResponseEntity<Void> removerConvidado(@AuthenticationPrincipal SessaoAdmin sessao,
                                                 @PathVariable UUID eventoId, @PathVariable UUID convidadoId) {
        removerConvidado.executar(sessao.contaId(), eventoId, convidadoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{eventoId}/guests")
    public Map<String, Long> removerTodosConvidados(@AuthenticationPrincipal SessaoAdmin sessao,
                                                    @PathVariable UUID eventoId) {
        long removidos = removerTodosConvidados.executar(sessao.contaId(), eventoId);
        return Map.of("removidos", removidos);
    }

    // ---- Importação CSV/XLSX ----

    @GetMapping("/{eventoId}/gifts/template")
    public ResponseEntity<byte[]> templatePresentes() {
        return csv("modelo-presentes.csv", "nome,quantidade,variante,link,imagem_url\n"
                + "Jogo de Panelas,1,inox,https://loja/produto,https://loja/img.jpg\n");
    }

    @GetMapping("/{eventoId}/guests/template")
    public ResponseEntity<byte[]> templateConvidados() {
        return csv("modelo-convidados.csv", "nome_completo,telefone,acompanhantes\n"
                + "Carlos Silva,+5511999990000,2\n");
    }

    @PostMapping("/{eventoId}/gifts/import")
    public ImportarListaPresentes.Resultado importarPresentes(@AuthenticationPrincipal SessaoAdmin sessao,
                                                              @PathVariable UUID eventoId,
                                                              @RequestParam("file") MultipartFile arquivo) {
        return importarPresentes.executar(sessao.contaId(), eventoId, bytes(arquivo), arquivo.getOriginalFilename());
    }

    @PostMapping("/{eventoId}/guests/import")
    public ImportarConvidados.Resultado importarConvidados(@AuthenticationPrincipal SessaoAdmin sessao,
                                                           @PathVariable UUID eventoId,
                                                           @RequestParam("file") MultipartFile arquivo) {
        return importarConvidados.executar(sessao.contaId(), eventoId, bytes(arquivo), arquivo.getOriginalFilename());
    }

    private static byte[] bytes(MultipartFile arquivo) {
        try {
            return arquivo.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Falha ao ler o arquivo enviado");
        }
    }

    private static ArquivoUpload arquivoUpload(MultipartFile arquivo) {
        return new ArquivoUpload(arquivo.getOriginalFilename(), arquivo.getContentType(), bytes(arquivo));
    }

    private static ResponseEntity<byte[]> csv(String nome, String conteudo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(conteudo.getBytes(StandardCharsets.UTF_8));
    }

    public record EventoRequest(short categoriaId, @NotBlank String slug, @NotBlank String nome,
                                String descricao, LocalDate dataEvento, boolean rsvpHabilitado,
                                Instant prazoRsvp, boolean presenteDinheiroHabilitado) {}

    public record AtualizarEventoRequest(@NotBlank String nome, String descricao, LocalDate dataEvento,
                                         String local, String mensagemCasal, String mensagemFinal,
                                         String mensagemAgradecimento, boolean rsvpHabilitado,
                                         Instant prazoRsvp, boolean presenteDinheiroHabilitado) {}

    public record TemaRequest(String paleta, String corDestaque, Integer overlay, String estiloAnimacao) {}

    public record SurpresaRequest(boolean ativo) {}

    public record PresenteRequest(@NotBlank String nome, String variante, @Positive int quantidade,
                                  String linkProduto, String imagemUrl) {}

    public record ConvidadoRequest(@NotBlank String nomeCompleto, @NotBlank String telefone,
                                   @PositiveOrZero int acompanhantesPermitidos) {}
}
