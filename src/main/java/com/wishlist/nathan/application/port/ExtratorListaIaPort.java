package com.wishlist.nathan.application.port;

import com.wishlist.nathan.application.dto.EntradaIa;
import com.wishlist.nathan.application.dto.ItemPresenteExtraido;

import java.util.List;

/**
 * Extrai itens de presente de uma lista já existente (imagem/PDF/texto) usando IA.
 * Os itens voltam sem preço ({@code precoEstimado} null); a estimativa é separada.
 * Implementado por um adapter que isola o provider (Anthropic).
 */
public interface ExtratorListaIaPort {

    List<ItemPresenteExtraido> extrair(EntradaIa entrada);
}
