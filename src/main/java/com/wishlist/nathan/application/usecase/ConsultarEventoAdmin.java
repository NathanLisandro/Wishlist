package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.domain.evento.Evento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/** Detalhe completo do evento para o painel do admin (campos editáveis + foto/slug). */
@Service
public class ConsultarEventoAdmin {

    private final AcessoEvento acessoEvento;

    public ConsultarEventoAdmin(AcessoEvento acessoEvento) {
        this.acessoEvento = acessoEvento;
    }

    @Transactional(readOnly = true)
    public Resultado executar(UUID contaId, UUID eventoId) {
        Evento e = acessoEvento.exigir(contaId, eventoId);
        boolean dono = e.getContaId().equals(contaId);
        return new Resultado(e.getId(), e.getSlug().valor(), e.getNome(), e.getDescricao(),
                e.getDataEvento(), e.getLocal(), e.getMensagemCasal(), e.getMensagemFinal(),
                e.getMensagemAgradecimento(), e.isRsvpHabilitado(), e.isPresenteDinheiroHabilitado(),
                e.getStatus().name(), e.getFotoUrl(), dono,
                dono ? e.getTokenConviteGestao() : null, // token só p/ o dono
                e.getPaletaTema().name(), e.getCorDestaque(), e.getFundoUrl(), e.getFundoOverlay(),
                e.getEstiloAnimacao().name());
    }

    public record Resultado(UUID id, String slug, String nome, String descricao, LocalDate dataEvento,
                            String local, String mensagemCasal, String mensagemFinal,
                            String mensagemAgradecimento, boolean rsvpHabilitado,
                            boolean presenteDinheiroHabilitado, String status, String fotoUrl,
                            boolean dono, String tokenConviteGestao,
                            String paletaTema, String corDestaque, String fundoUrl, Integer fundoOverlay,
                            String estiloAnimacao) {}
}
