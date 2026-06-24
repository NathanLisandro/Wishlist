package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.dto.ItemPresenteExtraido;
import com.wishlist.nathan.application.port.ImportJobRepositorioPort;
import com.wishlist.nathan.application.port.ItemPresenteRepositorioPort;
import com.wishlist.nathan.domain.comum.Dinheiro;
import com.wishlist.nathan.domain.importacao.StatusImportacao;
import com.wishlist.nathan.domain.importacao.TipoImportacao;
import com.wishlist.nathan.domain.presente.ItemPresente;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Materializa os itens revisados pelo casal (vindos da extração por IA), reusando
 * {@link AdicionarPresente} (mesma idempotência por natural_key, transação por item)
 * e gravando o preço estimado. Registra um import_job do tipo GIFTS para histórico.
 */
@Service
public class ConfirmarImportacaoIa {

    private final AcessoEvento acessoEvento;
    private final AdicionarPresente adicionar;
    private final ItemPresenteRepositorioPort itens;
    private final ImportJobRepositorioPort jobs;
    private final ObjectMapper mapper;

    public ConfirmarImportacaoIa(AcessoEvento acessoEvento, AdicionarPresente adicionar,
                                 ItemPresenteRepositorioPort itens, ImportJobRepositorioPort jobs,
                                 ObjectMapper mapper) {
        this.acessoEvento = acessoEvento;
        this.adicionar = adicionar;
        this.itens = itens;
        this.jobs = jobs;
        this.mapper = mapper;
    }

    public Resultado executar(UUID contaId, UUID eventoId, List<ItemPresenteExtraido> selecionados) {
        acessoEvento.exigirGestaoPresentes(contaId, eventoId);
        List<ItemPresenteExtraido> entrada = selecionados == null ? List.of() : selecionados;

        List<Map<String, Object>> erros = new ArrayList<>();
        int ok = 0;
        int n = 0;
        for (ItemPresenteExtraido item : entrada) {
            n++;
            try {
                int qtd = item.quantidade() == null || item.quantidade() < 1 ? 1 : item.quantidade();
                UUID id = adicionar.executar(contaId, eventoId, new AdicionarPresente.Comando(
                        item.nome(), item.variante(), qtd, item.linkProduto(), null));
                gravarPreco(id, item.precoEstimado());
                ok++;
            } catch (Exception e) {
                erros.add(erro(n, item.nome(), e.getMessage()));
            }
        }
        int falhas = entrada.size() - ok;
        String json = erros.isEmpty() ? null : mapper.writeValueAsString(erros);
        UUID jobId = jobs.registrar(new ImportJobRepositorioPort.Registro(
                eventoId, TipoImportacao.GIFTS, "IA", StatusImportacao.DONE,
                entrada.size(), ok, falhas, json));
        return new Resultado(jobId, entrada.size(), ok, falhas);
    }

    private void gravarPreco(UUID itemId, BigDecimal preco) {
        if (preco == null || preco.signum() <= 0) {
            return;
        }
        itens.buscarPorId(itemId).ifPresent(item -> {
            item.definirPrecoEstimado(Dinheiro.reais(preco));
            itens.salvar(item);
        });
    }

    private static Map<String, Object> erro(int linha, String nome, String motivo) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("linha", linha);
        m.put("item", nome);
        m.put("motivo", motivo);
        return m;
    }

    public record Resultado(UUID importJobId, int total, int ok, int falhas) {}
}
