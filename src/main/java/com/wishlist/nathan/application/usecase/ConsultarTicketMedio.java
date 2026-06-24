package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.application.port.ItemPresenteRepositorioPort;
import com.wishlist.nathan.domain.comum.Dinheiro;
import com.wishlist.nathan.domain.evento.Evento;
import com.wishlist.nathan.domain.presente.ItemPresente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Ticket médio da lista: média dos preços estimados dos presentes. Visível
 * APENAS para o dono do evento (não para co-gestores) — daí a checagem direta de
 * {@code contaId}, sem passar por {@link AcessoEvento}.
 */
@Service
public class ConsultarTicketMedio {

    private final EventoRepositorioPort eventos;
    private final ItemPresenteRepositorioPort itens;

    public ConsultarTicketMedio(EventoRepositorioPort eventos, ItemPresenteRepositorioPort itens) {
        this.eventos = eventos;
        this.itens = itens;
    }

    @Transactional(readOnly = true)
    public Resultado executar(UUID contaId, UUID eventoId) {
        Evento evento = eventos.buscarPorId(eventoId)
                // só o dono vê; e no modo surpresa nem o dono vê (a lista é segredo p/ ele)
                .filter(e -> e.getContaId().equals(contaId) && !e.isModoSurpresa())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));

        List<BigDecimal> precos = itens.listarDoEvento(evento.getId()).stream()
                .map(ItemPresente::getPrecoEstimado)
                .filter(java.util.Objects::nonNull)
                .map(Dinheiro::valor)
                .toList();

        if (precos.isEmpty()) {
            return new Resultado(null, 0);
        }
        BigDecimal soma = precos.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal media = soma.divide(BigDecimal.valueOf(precos.size()), 2, RoundingMode.HALF_EVEN);
        return new Resultado(media, precos.size());
    }

    public record Resultado(BigDecimal ticketMedio, int itensComPreco) {}
}
