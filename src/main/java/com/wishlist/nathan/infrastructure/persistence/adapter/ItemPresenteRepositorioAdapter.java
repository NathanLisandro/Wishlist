package com.wishlist.nathan.infrastructure.persistence.adapter;

import com.wishlist.nathan.application.port.ItemPresenteRepositorioPort;
import com.wishlist.nathan.domain.comum.Quantidade;
import com.wishlist.nathan.domain.presente.ItemPresente;
import com.wishlist.nathan.domain.presente.StatusPresente;
import com.wishlist.nathan.infrastructure.persistence.entity.ItemPresenteJpa;
import com.wishlist.nathan.infrastructure.persistence.repository.ItemPresenteJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ItemPresenteRepositorioAdapter implements ItemPresenteRepositorioPort {

    private final ItemPresenteJpaRepository repo;

    public ItemPresenteRepositorioAdapter(ItemPresenteJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public ItemPresente salvar(ItemPresente item) {
        ItemPresenteJpa jpa = new ItemPresenteJpa();
        jpa.setId(item.getId() != null ? item.getId() : UUID.randomUUID());
        jpa.setEventId(item.getEventoId());
        jpa.setName(item.getNome());
        jpa.setVariant(item.getVariante());
        jpa.setTotalQuantity(item.getQuantidadeTotal().valor());
        jpa.setProductLink(item.getLinkProduto());
        jpa.setImageUrl(item.getImagemUrl());
        jpa.setStatus(item.getStatus());
        jpa.setNaturalKey(item.getNaturalKey());
        return paraDominio(repo.save(jpa));
    }

    @Override
    public Optional<ItemPresente> buscarPorId(UUID id) {
        return repo.findById(id).map(this::paraDominio);
    }

    @Override
    public Optional<ItemPresente> buscarPorIdComLock(UUID id) {
        return repo.buscarComLock(id).map(this::paraDominio);
    }

    @Override
    public List<ItemPresente> listarDisponiveis(UUID eventoId) {
        return repo.findByEventIdAndStatus(eventoId, StatusPresente.AVAILABLE)
                .stream().map(this::paraDominio).toList();
    }

    @Override
    public List<ItemPresente> listarDoEvento(UUID eventoId) {
        return repo.findByEventIdAndStatusNotOrderByCreatedAt(eventoId, StatusPresente.HIDDEN)
                .stream().map(this::paraDominio).toList();
    }

    @Override
    public void apagar(UUID id) {
        repo.deleteById(id);
    }

    private ItemPresente paraDominio(ItemPresenteJpa j) {
        return new ItemPresente(j.getId(), j.getEventId(), j.getName(), j.getVariant(),
                new Quantidade(j.getTotalQuantity()), j.getProductLink(), j.getImageUrl(),
                j.getStatus(), j.getNaturalKey(), j.getCreatedAt());
    }
}
