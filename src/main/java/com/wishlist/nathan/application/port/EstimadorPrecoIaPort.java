package com.wishlist.nathan.application.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Estima o preço de mercado (BRL) de itens pelo nome, via IA. Devolve um mapa
 * nome → preço; nomes sem estimativa confiável podem ficar de fora.
 */
public interface EstimadorPrecoIaPort {

    Map<String, BigDecimal> estimar(List<String> nomes);
}
