package com.wishlist.nathan.domain.evento;

import com.wishlist.nathan.domain.comum.Slug;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Evento (casamento, chá de panela, etc.). Pertence a uma conta e expõe a lista
 * pública por {@link Slug}.
 */
public class Evento {

    private final UUID id;
    private final UUID contaId;
    private short categoriaId;
    private Slug slug;
    private String nome;
    private String descricao;
    private LocalDate dataEvento;
    private boolean rsvpHabilitado;
    private Instant prazoRsvp;          // opcional
    private boolean presenteDinheiroHabilitado;
    private StatusEvento status;
    private String fotoUrl;          // caminho público da foto do casal (opcional)
    private String local;            // "Onde" (opcional)
    private String mensagemCasal;    // recado/citação na landing (opcional)
    private String mensagemFinal;    // frase de despedida no rodapé (opcional)
    private String mensagemAgradecimento; // exibida após confirmar os presentes (opcional)
    private final Instant criadoEm;

    public Evento(UUID id, UUID contaId, short categoriaId, Slug slug, String nome, String descricao,
                  LocalDate dataEvento, boolean rsvpHabilitado, Instant prazoRsvp,
                  boolean presenteDinheiroHabilitado, StatusEvento status, Instant criadoEm) {
        if (contaId == null) {
            throw new IllegalArgumentException("contaId é obrigatório");
        }
        if (slug == null) {
            throw new IllegalArgumentException("slug é obrigatório");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome é obrigatório");
        }
        this.id = id;
        this.contaId = contaId;
        this.categoriaId = categoriaId;
        this.slug = slug;
        this.nome = nome;
        this.descricao = descricao;
        this.dataEvento = dataEvento;
        this.rsvpHabilitado = rsvpHabilitado;
        this.prazoRsvp = prazoRsvp;
        this.presenteDinheiroHabilitado = presenteDinheiroHabilitado;
        this.status = status == null ? StatusEvento.DRAFT : status;
        this.criadoEm = criadoEm;
    }

    /**
     * Atualiza os campos editáveis pelo casal (não mexe em id/conta/slug/categoria).
     * Usado por {@code AtualizarEvento}.
     */
    public void editar(String nome, String descricao, LocalDate dataEvento, String local,
                       String mensagemCasal, String mensagemFinal, String mensagemAgradecimento,
                       boolean rsvpHabilitado, Instant prazoRsvp, boolean presenteDinheiroHabilitado) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome é obrigatório");
        }
        this.nome = nome;
        this.descricao = descricao;
        this.dataEvento = dataEvento;
        this.local = local;
        this.mensagemCasal = mensagemCasal;
        this.mensagemFinal = mensagemFinal;
        this.mensagemAgradecimento = mensagemAgradecimento;
        this.rsvpHabilitado = rsvpHabilitado;
        this.prazoRsvp = prazoRsvp;
        this.presenteDinheiroHabilitado = presenteDinheiroHabilitado;
    }

    public void definirFoto(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public void publicar() {
        this.status = StatusEvento.PUBLISHED;
    }

    public void encerrar() {
        this.status = StatusEvento.CLOSED;
    }

    public boolean publicado() {
        return status == StatusEvento.PUBLISHED;
    }

    /** RSVP aberto se habilitado e dentro do prazo (quando houver prazo). */
    public boolean rsvpAberto(Instant agora) {
        if (!rsvpHabilitado) {
            return false;
        }
        return prazoRsvp == null || !agora.isAfter(prazoRsvp);
    }

    public UUID getId() { return id; }
    public UUID getContaId() { return contaId; }
    public short getCategoriaId() { return categoriaId; }
    public Slug getSlug() { return slug; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public LocalDate getDataEvento() { return dataEvento; }
    public boolean isRsvpHabilitado() { return rsvpHabilitado; }
    public Instant getPrazoRsvp() { return prazoRsvp; }
    public boolean isPresenteDinheiroHabilitado() { return presenteDinheiroHabilitado; }
    public StatusEvento getStatus() { return status; }
    public String getFotoUrl() { return fotoUrl; }
    public String getLocal() { return local; }
    public String getMensagemCasal() { return mensagemCasal; }
    public String getMensagemFinal() { return mensagemFinal; }
    public String getMensagemAgradecimento() { return mensagemAgradecimento; }
    public Instant getCriadoEm() { return criadoEm; }

    /** Reidrata os campos opcionais ao reconstruir a partir do banco. */
    public void hidratarDetalhes(String fotoUrl, String local, String mensagemCasal,
                                 String mensagemFinal, String mensagemAgradecimento) {
        this.fotoUrl = fotoUrl;
        this.local = local;
        this.mensagemCasal = mensagemCasal;
        this.mensagemFinal = mensagemFinal;
        this.mensagemAgradecimento = mensagemAgradecimento;
    }
}
