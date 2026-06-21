package com.wishlist.nathan.infrastructure.persistence.adapter;

import com.wishlist.nathan.application.port.RegistroAcessoPort;
import com.wishlist.nathan.domain.convidado.AcaoAcessoConvidado;
import com.wishlist.nathan.infrastructure.persistence.entity.GuestAccessLogJpa;
import com.wishlist.nathan.infrastructure.persistence.repository.GuestAccessLogJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class RegistroAcessoAdapter implements RegistroAcessoPort {

    private final GuestAccessLogJpaRepository repo;

    public RegistroAcessoAdapter(GuestAccessLogJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public void registrar(UUID eventoId, UUID convidadoId, byte[] phoneHash,
                          AcaoAcessoConvidado acao, String ip, String userAgent) {
        GuestAccessLogJpa jpa = new GuestAccessLogJpa();
        jpa.setId(UUID.randomUUID());
        jpa.setEventId(eventoId);
        jpa.setGuestId(convidadoId);
        jpa.setPhoneHash(phoneHash);
        jpa.setAction(acao);
        jpa.setIp(ip);
        jpa.setUserAgent(userAgent);
        repo.save(jpa);
    }

    @Override
    public List<AcessoRecente> listarRecentes(UUID eventoId) {
        return repo.findTop50ByEventIdOrderByCreatedAtDesc(eventoId).stream()
                .map(j -> new AcessoRecente(j.getAction(), j.getGuestId(), j.getIp(), j.getCreatedAt()))
                .toList();
    }
}
