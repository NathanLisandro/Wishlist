package com.wishlist.nathan.application.dto;

import java.util.UUID;

/** Identidade autenticada de um admin (extraída do token de sessão). */
public record SessaoAdmin(UUID contaId) {}
