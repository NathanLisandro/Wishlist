package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.port.RegistroAcessoPort;
import com.wishlist.nathan.application.port.RsvpRepositorioPort;
import com.wishlist.nathan.domain.convidado.AcaoAcessoConvidado;
import com.wishlist.nathan.domain.rsvp.Rsvp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Cancela o RSVP (dela + acompanhantes) a qualquer momento. Idempotente. */
@Service
public class CancelarPresenca {

    private final RsvpRepositorioPort rsvps;
    private final RegistroAcessoPort acessos;

    public CancelarPresenca(RsvpRepositorioPort rsvps, RegistroAcessoPort acessos) {
        this.rsvps = rsvps;
        this.acessos = acessos;
    }

    @Transactional
    public void executar(Comando cmd) {
        rsvps.buscarPorEventoEConvidado(cmd.eventoId(), cmd.convidadoId()).ifPresent(rsvp -> {
            rsvp.cancelar(Instant.now());
            rsvps.salvar(rsvp);
        });
        acessos.registrar(cmd.eventoId(), cmd.convidadoId(), null,
                AcaoAcessoConvidado.RSVP_CANCEL, cmd.ip(), cmd.userAgent());
    }

    public record Comando(UUID eventoId, UUID convidadoId, String ip, String userAgent) {}
}
