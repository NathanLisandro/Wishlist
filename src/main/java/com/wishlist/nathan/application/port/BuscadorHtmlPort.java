package com.wishlist.nathan.application.port;

/**
 * Busca o conteúdo textual de uma URL pública (lista de presentes de outro site),
 * já sem HTML, para alimentar a extração por IA. A implementação protege contra
 * SSRF (bloqueia IPs privados/loopback, limita tamanho e tempo).
 */
public interface BuscadorHtmlPort {

    String buscarTexto(String url);
}
