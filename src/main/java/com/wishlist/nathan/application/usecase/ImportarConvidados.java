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
 * Importa convidados de CSV/XLSX (colunas: nome_completo, telefone, acompanhantes).
 * Cada linha em sua transação (reusa AdicionarConvidado). Idempotente: dedup por
 * (event_id, phone_hash).
 */
@Service
public class ImportarConvidados {

    private final LeitorPlanilha leitor;
    private final AdicionarConvidado adicionar;
    private final ImportJobRepositorioPort jobs;
    private final ObjectMapper mapper;

    public ImportarConvidados(LeitorPlanilha leitor, AdicionarConvidado adicionar,
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
                int acomp = ImportUtil.inteiroOpcional(linha.get("acompanhantes"));
                adicionar.executar(contaId, eventoId, new AdicionarConvidado.Comando(
                        linha.getOrDefault("nome_completo", ""), linha.getOrDefault("telefone", ""), acomp));
                ok++;
            } catch (Exception e) {
                erros.add(erro(n, e.getMessage()));
            }
        }
        int falhas = linhas.size() - ok;
        String json = erros.isEmpty() ? null : mapper.writeValueAsString(erros);
        UUID jobId = jobs.registrar(new ImportJobRepositorioPort.Registro(
                eventoId, TipoImportacao.GUESTS, arquivo, StatusImportacao.DONE,
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
