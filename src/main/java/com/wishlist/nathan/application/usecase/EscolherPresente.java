package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.ItemPresenteRepositorioPort;
import com.wishlist.nathan.application.port.RegistroAcessoPort;
import com.wishlist.nathan.application.port.ReservaPresenteRepositorioPort;
import com.wishlist.nathan.domain.comum.ChaveIdempotencia;
import com.wishlist.nathan.domain.convidado.AcaoAcessoConvidado;
import com.wishlist.nathan.domain.presente.ItemPresente;
import com.wishlist.nathan.domain.presente.ReservaPresente;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Reserva idempotente de presente, sem oversell (SDD §6.1):
 * lock de linha no item + contagem de reservas ativas + Idempotency-Key único.
 */
@Service
public class EscolherPresente {

    private final ItemPresenteRepositorioPort itens;
    private final ReservaPresenteRepositorioPort reservas;
    private final RegistroAcessoPort acessos;

    public EscolherPresente(ItemPresenteRepositorioPort itens, ReservaPresenteRepositorioPort reservas,
                            RegistroAcessoPort acessos) {
        this.itens = itens;
        this.reservas = reservas;
        this.acessos = acessos;
    }

    @Transactional
    public Resultado executar(Comando cmd) {
        ChaveIdempotencia chave = new ChaveIdempotencia(cmd.chaveIdempotencia());

        // 1) Replay exato da mesma requisição -> devolve o mesmo resultado.
        Optional<ReservaPresente> porChave = reservas.buscarPorChaveIdempotencia(chave.valor());
        if (porChave.isPresent()) {
            return Resultado.de(porChave.get(), false);
        }

        // 2) Lock de linha no item (serializa cliques concorrentes).
        ItemPresente item = itens.buscarPorIdComLock(cmd.itemId())
                .filter(i -> i.getEventoId().equals(cmd.eventoId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Presente não encontrado"));

        // 3) Já reservado por este convidado -> idempotente por (item, convidado).
        Optional<ReservaPresente> jaReservado =
                reservas.buscarAtivaPorItemEConvidado(item.getId(), cmd.convidadoId());
        if (jaReservado.isPresent()) {
            return Resultado.de(jaReservado.get(), false);
        }

        // 4) Regra de domínio: conta ativas e reserva (pode esgotar -> SOLD_OUT).
        int ativas = reservas.contarAtivasPorItem(item.getId());
        ReservaPresente reserva = item.reservar(cmd.convidadoId(), ativas, chave);

        try {
            ReservaPresente salva = reservas.salvar(reserva);
            itens.salvar(item); // persiste eventual mudança de status (SOLD_OUT)
            acessos.registrar(cmd.eventoId(), cmd.convidadoId(), null,
                    AcaoAcessoConvidado.CLAIM_GIFT, cmd.ip(), cmd.userAgent());
            return Resultado.de(salva, !item.disponivel());
        } catch (DataIntegrityViolationException e) {
            // Corrida: outra requisição inseriu a mesma chave/claim. Devolve o existente.
            return reservas.buscarPorChaveIdempotencia(chave.valor())
                    .or(() -> reservas.buscarAtivaPorItemEConvidado(item.getId(), cmd.convidadoId()))
                    .map(r -> Resultado.de(r, false))
                    .orElseThrow(() -> e);
        }
    }

    public record Comando(UUID eventoId, UUID convidadoId, UUID itemId,
                          String chaveIdempotencia, String ip, String userAgent) {}

    public record Resultado(UUID reservaId, UUID itemId, boolean esgotou) {
        static Resultado de(ReservaPresente r, boolean esgotou) {
            return new Resultado(r.getId(), r.getItemPresenteId(), esgotou);
        }
    }
}
