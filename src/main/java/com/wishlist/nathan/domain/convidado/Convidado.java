package com.wishlist.nathan.domain.convidado;

import com.wishlist.nathan.domain.comum.NomeCriptografado;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

/**
 * Convidado de um evento. PII (nome/telefone) cifrada; o lookup por telefone é
 * sempre via {@code phoneHash} (HMAC), nunca pelo número em claro.
 */
public class Convidado {

    private final UUID id;
    private final UUID eventoId;
    private NomeCriptografado nomeCifrado;
    private NomeCriptografado telefoneCifrado;
    private final byte[] phoneHash;          // HMAC-SHA256 do telefone
    private short acompanhantesPermitidos;
    private final Instant criadoEm;

    public Convidado(UUID id, UUID eventoId, NomeCriptografado nomeCifrado,
                     NomeCriptografado telefoneCifrado, byte[] phoneHash,
                     short acompanhantesPermitidos, Instant criadoEm) {
        if (eventoId == null) {
            throw new IllegalArgumentException("eventoId é obrigatório");
        }
        if (nomeCifrado == null || telefoneCifrado == null) {
            throw new IllegalArgumentException("Nome e telefone cifrados são obrigatórios");
        }
        if (phoneHash == null || phoneHash.length == 0) {
            throw new IllegalArgumentException("phoneHash é obrigatório");
        }
        if (acompanhantesPermitidos < 0) {
            throw new IllegalArgumentException("acompanhantesPermitidos não pode ser negativo");
        }
        this.id = id;
        this.eventoId = eventoId;
        this.nomeCifrado = nomeCifrado;
        this.telefoneCifrado = telefoneCifrado;
        this.phoneHash = phoneHash.clone();
        this.acompanhantesPermitidos = acompanhantesPermitidos;
        this.criadoEm = criadoEm;
    }

    public boolean podeLevarAcompanhantes() {
        return acompanhantesPermitidos > 0;
    }

    public UUID getId() { return id; }
    public UUID getEventoId() { return eventoId; }
    public NomeCriptografado getNomeCifrado() { return nomeCifrado; }
    public NomeCriptografado getTelefoneCifrado() { return telefoneCifrado; }
    public byte[] getPhoneHash() { return phoneHash.clone(); }
    public short getAcompanhantesPermitidos() { return acompanhantesPermitidos; }
    public Instant getCriadoEm() { return criadoEm; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Convidado outro && Arrays.equals(phoneHash, outro.phoneHash)
                && eventoId.equals(outro.eventoId);
    }

    @Override
    public int hashCode() {
        return 31 * eventoId.hashCode() + Arrays.hashCode(phoneHash);
    }
}
