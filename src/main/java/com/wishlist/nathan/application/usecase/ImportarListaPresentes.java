package com.wishlist.nathan.application.usecase;

import com.wishlist.nathan.application.port.ImportJobRepositorioPort;
import com.wishlist.nathan.domain.importacao.StatusImportacao;
import com.wishlist.nathan.domain.importacao.TipoImportacao;
import com.wishlist.nathan.infrastructure.file.LeitorPlanilha;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Importa presentes de CSV/XLSX (colunas: nome, quantidade, variante, link, imagem_url).
 * Cada linha é processada em sua própria transação (reusa AdicionarPresente), com
 * relatório de erros por linha. Idempotente: duplicados (natural_key) não dobram.
 */
@Service
public class ImportarListaPresentes {

    private final LeitorPlanilha leitor;
    private final AdicionarPresente adicionar;
    private final ImportJobRepositorioPort jobs;
    private final ObjectMapper mapper;

    public ImportarListaPresentes(LeitorPlanilha leitor, AdicionarPresente adicionar,
                                  ImportJobRepositorioPort jobs, ObjectMapper mapper) {
        this.leitor = leitor;
        this.adicionar = adicionar;
        this.jobs = jobs;
        this.mapper = mapper;
    }

    public Resultado executar(UUID contaId, UUID eventoId, byte[] conteudo, String arquivo) {
        List<Map<String, String>> linhas = leitor.ler(conteudo, arquivo);
        List<Map<String, Object>> erros = new ArrayList<>();
        int ok = 0;
        int n = 0;
        for (Map<String, String> linha : linhas) {
            n++;
            try {
                int qtd = ImportUtil.inteiroObrigatorio(linha.get("quantidade"), "quantidade");
                adicionar.executar(contaId, eventoId, new AdicionarPresente.Comando(
                        linha.getOrDefault("nome", ""), ImportUtil.vazioParaNull(linha.get("variante")),
                        qtd, ImportUtil.vazioParaNull(linha.get("link")),
                        ImportUtil.vazioParaNull(linha.get("imagem_url"))));
                ok++;
            } catch (Exception e) {
                erros.add(erro(n, e.getMessage()));
            }
        }
        int falhas = linhas.size() - ok;
        String json = erros.isEmpty() ? null : mapper.writeValueAsString(erros);
        UUID jobId = jobs.registrar(new ImportJobRepositorioPort.Registro(
                eventoId, TipoImportacao.GIFTS, arquivo, StatusImportacao.DONE,
                linhas.size(), ok, falhas, json));
        return new Resultado(jobId, linhas.size(), ok, falhas);
    }

    private static Map<String, Object> erro(int linha, String motivo) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("linha", linha);
        m.put("motivo", motivo);
        return m;
    }

    public record Resultado(UUID importJobId, int total, int ok, int falhas) {}
}
