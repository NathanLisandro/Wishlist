package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.dto.FiltroRsvp;
import com.wishlist.nathan.application.dto.Pagina;
import com.wishlist.nathan.application.port.ConvidadoRepositorioPort;
import com.wishlist.nathan.application.port.CriptografiaPort;
import com.wishlist.nathan.application.port.RsvpRepositorioPort;
import com.wishlist.nathan.domain.convidado.Convidado;
import com.wishlist.nathan.domain.evento.Evento;
import com.wishlist.nathan.domain.rsvp.Rsvp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Lista de presença do painel: convidados do evento com o status do RSVP e os
 * acompanhantes confirmados. O nome é decifrado SÓ aqui (painel autorizado);
 * o telefone nunca é exposto.
 */
@Service
public class ListarConvidados {

    private final AcessoEvento acessoEvento;
    private final ConvidadoRepositorioPort convidados;
    private final RsvpRepositorioPort rsvps;
    private final CriptografiaPort cripto;

    public ListarConvidados(AcessoEvento acessoEvento, ConvidadoRepositorioPort convidados,
                            RsvpRepositorioPort rsvps, CriptografiaPort cripto) {
        this.acessoEvento = acessoEvento;
        this.convidados = convidados;
        this.rsvps = rsvps;
        this.cripto = cripto;
    }

    @Transactional(readOnly = true)
    public List<ConvidadoPainel> executar(UUID contaId, UUID eventoId) {
        Evento evento = acessoEvento.exigir(contaId, eventoId);
        return convidados.listarPorEvento(evento.getId()).stream()
                .map(c -> montar(evento.getId(), c)).toList();
    }

    @Transactional(readOnly = true)
    public Pagina<ConvidadoPainel> executarPaginado(UUID contaId, UUID eventoId, FiltroRsvp filtro,
                                                    int page, int size) {
        Evento evento = acessoEvento.exigir(contaId, eventoId);
        return convidados.listarPorEventoPaginado(evento.getId(), filtro, page, size)
                .mapear(c -> montar(evento.getId(), c));
    }

    private ConvidadoPainel montar(UUID eventoId, Convidado c) {
        Rsvp rsvp = rsvps.buscarPorEventoEConvidado(eventoId, c.getId()).orElse(null);
        String status = rsvp == null ? "PENDING" : rsvp.getStatus().name();
        int acompanhantes = rsvp == null ? 0 : rsvp.getAcompanhantes().size();
        return new ConvidadoPainel(c.getId(), nomeDe(c), decifrar(c.getTelefoneCifrado()),
                c.getAcompanhantesPermitidos(), status, acompanhantes);
    }

    private String nomeDe(Convidado c) {
        return decifrar(c.getNomeCifrado());
    }

    private String decifrar(com.wishlist.nathan.domain.comum.NomeCriptografado cifrado) {
        try {
            return cripto.descriptografar(cifrado.conteudo());
        } catch (RuntimeException e) {
            return "—";
        }
    }

    public record ConvidadoPainel(UUID id, String nome, String telefone, int acompanhantesPermitidos,
                                  String statusRsvp, int acompanhantesConfirmados) {}
}
