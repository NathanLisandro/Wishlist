package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ItemPresenteRepositorioPort;
import com.wishlist.nathan.application.port.RegistroAcessoPort;
import com.wishlist.nathan.application.port.ReservaPresenteRepositorioPort;
import com.wishlist.nathan.domain.convidado.AcaoAcessoConvidado;
import com.wishlist.nathan.domain.presente.ItemPresente;
import com.wishlist.nathan.domain.presente.ReservaPresente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Cancela a reserva do convidado; item esgotado volta a ficar disponível. */
@Service
public class CancelarPresente {

    private final ItemPresenteRepositorioPort itens;
    private final ReservaPresenteRepositorioPort reservas;
    private final RegistroAcessoPort acessos;

    public CancelarPresente(ItemPresenteRepositorioPort itens, ReservaPresenteRepositorioPort reservas,
                            RegistroAcessoPort acessos) {
        this.itens = itens;
        this.reservas = reservas;
        this.acessos = acessos;
    }

    @Transactional
    public void executar(Comando cmd) {
        ItemPresente item = itens.buscarPorIdComLock(cmd.itemId())
                .filter(i -> i.getEventoId().equals(cmd.eventoId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Presente não encontrado"));

        ReservaPresente reserva = reservas.buscarAtivaPorItemEConvidado(item.getId(), cmd.convidadoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Você não tem reserva ativa neste presente"));

        reserva.cancelar(Instant.now());
        reservas.salvar(reserva);
        item.liberarUnidade();
        itens.salvar(item);

        acessos.registrar(cmd.eventoId(), cmd.convidadoId(), null,
                AcaoAcessoConvidado.CANCEL_CLAIM, cmd.ip(), cmd.userAgent());
    }

    public record Comando(UUID eventoId, UUID convidadoId, UUID itemId, String ip, String userAgent) {}
}
