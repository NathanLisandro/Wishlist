package com.wishlist.nathan.infrastructure.persistence.adapter;

import com.wishlist.nathan.application.port.EventoRepositorioPort;
import com.wishlist.nathan.domain.comum.Slug;
import com.wishlist.nathan.domain.evento.Evento;
import com.wishlist.nathan.infrastructure.persistence.entity.EventoJpa;
import com.wishlist.nathan.infrastructure.persistence.repository.EventoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class EventoRepositorioAdapter implements EventoRepositorioPort {

    private final EventoJpaRepository repo;

    public EventoRepositorioAdapter(EventoJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Evento salvar(Evento evento) {
        EventoJpa jpa = new EventoJpa();
        jpa.setId(evento.getId() != null ? evento.getId() : UUID.randomUUID());
        jpa.setAccountId(evento.getContaId());
        jpa.setCategoryId(evento.getCategoriaId());
        jpa.setSlug(evento.getSlug().valor());
        jpa.setName(evento.getNome());
        jpa.setDescription(evento.getDescricao());
        jpa.setEventDate(evento.getDataEvento());
        jpa.setRsvpEnabled(evento.isRsvpHabilitado());
        jpa.setRsvpDeadline(evento.getPrazoRsvp());
        jpa.setMoneyGiftEnabled(evento.isPresenteDinheiroHabilitado());
        jpa.setStatus(evento.getStatus());
        jpa.setPhotoUrl(evento.getFotoUrl());
        jpa.setLocation(evento.getLocal());
        jpa.setCoupleMessage(evento.getMensagemCasal());
        jpa.setClosingMessage(evento.getMensagemFinal());
        jpa.setThankYouMessage(evento.getMensagemAgradecimento());
        return paraDominio(repo.save(jpa));
    }

    @Override
    public Optional<Evento> buscarPorId(UUID id) {
        return repo.findById(id).map(this::paraDominio);
    }

    @Override
    public Optional<Evento> buscarPorSlug(String slug) {
        return repo.findFirstBySlug(slug).map(this::paraDominio);
    }

    @Override
    public List<Evento> listarPorConta(UUID contaId) {
        return repo.findByAccountId(contaId).stream().map(this::paraDominio).toList();
    }

    @Override
    public boolean existeSlugNaConta(UUID contaId, String slug) {
        return repo.existsByAccountIdAndSlug(contaId, slug);
    }

    private Evento paraDominio(EventoJpa j) {
        Evento evento = new Evento(j.getId(), j.getAccountId(), j.getCategoryId(), new Slug(j.getSlug()),
                j.getName(), j.getDescription(), j.getEventDate(), j.isRsvpEnabled(),
                j.getRsvpDeadline(), j.isMoneyGiftEnabled(), j.getStatus(), j.getCreatedAt());
        evento.hidratarDetalhes(j.getPhotoUrl(), j.getLocation(), j.getCoupleMessage(),
                j.getClosingMessage(), j.getThankYouMessage());
        return evento;
    }
}
