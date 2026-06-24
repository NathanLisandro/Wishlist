package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.evento.EstiloAnimacao;
import com.wishlist.nathan.domain.evento.Evento;
import com.wishlist.nathan.domain.evento.PaletaTema;
import com.wishlist.nathan.domain.evento.Tema;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Aplica a personalização visual do site escolhida pelo casal: paleta pré-definida,
 * cor de destaque opcional e opacidade do overlay sobre o fundo. Acessível ao dono
 * ou co-gestor (gerência do evento).
 */
@Service
public class PersonalizarTema {

    private final EventoRepositorioPort eventos;
    private final AcessoEvento acessoEvento;

    public PersonalizarTema(EventoRepositorioPort eventos, AcessoEvento acessoEvento) {
        this.eventos = eventos;
        this.acessoEvento = acessoEvento;
    }

    @Transactional
    public void executar(UUID contaId, UUID eventoId, Comando cmd) {
        Evento evento = acessoEvento.exigir(contaId, eventoId);
        Tema tema = new Tema(PaletaTema.ofNullable(cmd.paleta()), cmd.corDestaque(), cmd.overlay(),
                EstiloAnimacao.ofNullable(cmd.estiloAnimacao()));
        evento.personalizarTema(tema);
        eventos.salvar(evento);
    }

    public record Comando(String paleta, String corDestaque, Integer overlay, String estiloAnimacao) {}
}
