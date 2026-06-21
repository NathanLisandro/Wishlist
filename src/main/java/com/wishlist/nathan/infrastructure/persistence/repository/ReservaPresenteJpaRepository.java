package com.wishlist.nathan.infrastructure.persistence.repository;

import com.wishlist.nathan.domain.presente.StatusReserva;
import com.wishlist.nathan.infrastructure.persistence.entity.ReservaPresenteJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaPresenteJpaRepository extends JpaRepository<ReservaPresenteJpa, UUID> {

    int countByGiftItemIdAndStatus(UUID giftItemId, StatusReserva status);

    /** Ids de itens reservados (ativos) pelo convidado dentro de um evento. */
    @Query("select c.giftItemId from ReservaPresenteJpa c where c.guestId = :convidadoId "
            + "and c.status = :status and c.giftItemId in "
            + "(select i.id from ItemPresenteJpa i where i.eventId = :eventoId)")
    List<UUID> listarItensReservados(@Param("eventoId") UUID eventoId,
                                     @Param("convidadoId") UUID convidadoId,
                                     @Param("status") StatusReserva status);

    Optional<ReservaPresenteJpa> findByGiftItemIdAndGuestIdAndStatus(
            UUID giftItemId, UUID guestId, StatusReserva status);

    Optional<ReservaPresenteJpa> findByIdempotencyKey(String idempotencyKey);

    /** Total de reservas ativas (status passado como parâmetro) dos itens do evento. */
    @Query("select count(c) from ReservaPresenteJpa c where c.status = :status and c.giftItemId in "
            + "(select i.id from ItemPresenteJpa i where i.eventId = :eventoId)")
    long contarAtivasPorEvento(@Param("eventoId") UUID eventoId, @Param("status") StatusReserva status);
}
