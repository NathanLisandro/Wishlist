package com.wishlist.nathan.application.dto;

/**
 * Dados de um arquivo enviado pelo cliente (multipart), desacoplados do
 * {@code MultipartFile} do Spring para não vazar tipos de framework para a camada
 * de aplicação. Usado em uploads (foto, fundo, imagem de presente).
 */
public record ArquivoUpload(String nomeOriginal, String contentType, byte[] conteudo) {}
