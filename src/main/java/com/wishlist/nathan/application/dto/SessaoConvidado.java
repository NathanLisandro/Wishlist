package com.wishlist.nathan.application.dto;

import java.util.UUID;

/** Identidade de um convidado, escopada a um evento (extraída do token). */
public record SessaoConvidado(UUID eventoId, UUID convidadoId) {}
