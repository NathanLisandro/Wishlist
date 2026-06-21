package com.wishlist.nathan.application.port;

import com.wishlist.nathan.domain.pagamento.PresenteDinheiro;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface MoneyGiftRepositorioPort {

    PresenteDinheiro salvar(PresenteDinheiro presente);

    Optional<PresenteDinheiro> buscarPorId(UUID id);

    Optional<PresenteDinheiro> buscarPorMpPaymentId(String mpPaymentId);

    /** Soma dos presentes em dinheiro APROVADOS do evento (dashboard). */
    BigDecimal somarAprovadosPorEvento(UUID eventoId);

    long contarAprovadosPorEvento(UUID eventoId);
}
