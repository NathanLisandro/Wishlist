package com.wishlist.nathan.infrastructure.persistence.adapter;

import com.wishlist.nathan.application.port.ReservaPresenteRepositorioPort;
import com.wishlist.nathan.domain.comum.ChaveIdempotencia;
import com.wishlist.nathan.domain.presente.ReservaPresente;
import com.wishlist.nathan.domain.presente.StatusReserva;
import com.wishlist.nathan.infrastructure.persistence.entity.ReservaPresenteJpa;
import com.wishlist.nathan.infrastructure.persistence.repository.ReservaPresenteJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ReservaPresenteRepositorioAdapter implements ReservaPresenteRepositorioPort {

    private final ReservaPresenteJpaRepository repo;

    public ReservaPresenteRepositorioAdapter(ReservaPresenteJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public ReservaPresente salvar(ReservaPresente reserva) {
        ReservaPresenteJpa jpa = new ReservaPresenteJpa();
        jpa.setId(reserva.getId() != null ? reserva.getId() : UUID.randomUUID());
        jpa.setGiftItemId(reserva.getItemPresenteId());
        jpa.setGuestId(reserva.getConvidadoId());
        jpa.setStatus(reserva.getStatus());
        jpa.setIdempotencyKey(reserva.getChaveIdempotencia().valor());
        jpa.setCancelledAt(reserva.getCanceladaEm());
        return paraDominio(repo.save(jpa));
    }

    @Override
    public int contarAtivasPorItem(UUID itemPresenteId) {
        return repo.countByGiftItemIdAndStatus(itemPresenteId, StatusReserva.CLAIMED);
    }

    @Override
    public List<UUID> listarItensReservadosPorConvidado(UUID eventoId, UUID convidadoId) {
        return repo.listarItensReservados(eventoId, convidadoId, StatusReserva.CLAIMED);
    }

    @Override
    public Optional<ReservaPresente> buscarAtivaPorItemEConvidado(UUID itemPresenteId, UUID convidadoId) {
        return repo.findByGiftItemIdAndGuestIdAndStatus(itemPresenteId, convidadoId, StatusReserva.CLAIMED)
                .map(this::paraDominio);
    }

    @Override
    public Optional<ReservaPresente> buscarPorChaveIdempotencia(String chave) {
        return repo.findByIdempotencyKey(chave).map(this::paraDominio);
    }

    @Override
    public long contarAtivasPorEvento(UUID eventoId) {
        return repo.contarAtivasPorEvento(eventoId, StatusReserva.CLAIMED);
    }

    private ReservaPresente paraDominio(ReservaPresenteJpa j) {
        return new ReservaPresente(j.getId(), j.getGiftItemId(), j.getGuestId(), j.getStatus(),
                new ChaveIdempotencia(j.getIdempotencyKey()), j.getCreatedAt(), j.getCancelledAt());
    }
}
