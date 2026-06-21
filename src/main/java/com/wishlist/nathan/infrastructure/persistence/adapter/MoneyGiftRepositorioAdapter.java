package com.wishlist.nathan.infrastructure.persistence.adapter;

import com.wishlist.nathan.application.port.MoneyGiftRepositorioPort;
import com.wishlist.nathan.domain.comum.Dinheiro;
import com.wishlist.nathan.domain.pagamento.PresenteDinheiro;
import com.wishlist.nathan.domain.pagamento.StatusPresenteDinheiro;
import com.wishlist.nathan.infrastructure.persistence.entity.MoneyGiftJpa;
import com.wishlist.nathan.infrastructure.persistence.repository.MoneyGiftJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

@Component
public class MoneyGiftRepositorioAdapter implements MoneyGiftRepositorioPort {

    private final MoneyGiftJpaRepository repo;

    public MoneyGiftRepositorioAdapter(MoneyGiftJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public PresenteDinheiro salvar(PresenteDinheiro p) {
        MoneyGiftJpa jpa = repo.findById(p.getId() != null ? p.getId() : UUID.randomUUID())
                .orElseGet(MoneyGiftJpa::new);
        if (jpa.getId() == null) {
            jpa.setId(p.getId() != null ? p.getId() : UUID.randomUUID());
        }
        jpa.setEventId(p.getEventoId());
        jpa.setGuestId(p.getConvidadoId());
        jpa.setAmount(p.getValor().valor());
        jpa.setCurrency(p.getValor().moeda().getCurrencyCode());
        jpa.setMessage(p.getMensagem());
        jpa.setMpPreferenceId(p.getMpPreferenceId());
        jpa.setMpPaymentId(p.getMpPaymentId());
        jpa.setStatus(p.getStatus());
        return paraDominio(repo.save(jpa));
    }

    @Override
    public Optional<PresenteDinheiro> buscarPorId(UUID id) {
        return repo.findById(id).map(this::paraDominio);
    }

    @Override
    public Optional<PresenteDinheiro> buscarPorMpPaymentId(String mpPaymentId) {
        return repo.findByMpPaymentId(mpPaymentId).map(this::paraDominio);
    }

    @Override
    public BigDecimal somarAprovadosPorEvento(UUID eventoId) {
        return repo.somar(eventoId, StatusPresenteDinheiro.APPROVED);
    }

    @Override
    public long contarAprovadosPorEvento(UUID eventoId) {
        return repo.countByEventIdAndStatus(eventoId, StatusPresenteDinheiro.APPROVED);
    }

    private PresenteDinheiro paraDominio(MoneyGiftJpa j) {
        Dinheiro valor = new Dinheiro(j.getAmount(), Currency.getInstance(j.getCurrency()));
        return new PresenteDinheiro(j.getId(), j.getEventId(), j.getGuestId(), valor, j.getMessage(),
                j.getMpPreferenceId(), j.getMpPaymentId(), j.getStatus(), j.getCreatedAt());
    }
}
