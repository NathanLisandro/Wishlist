package com.wishlist.nathan.infrastructure.file;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lê planilhas de importação para uma lista de linhas (cabeçalho -&gt; valor),
 * com chaves normalizadas (minúsculas, sem espaços). CSV via Commons CSV; XLSX via POI.
 */
@Component
public class LeitorPlanilha {

    public List<Map<String, String>> ler(byte[] conteudo, String nomeArquivo) {
        String nome = nomeArquivo == null ? "" : nomeArquivo.toLowerCase();
        if (nome.endsWith(".xlsx") || nome.endsWith(".xls")) {
            return lerXlsx(conteudo);
        }
        return lerCsv(conteudo);
    }

    private List<Map<String, String>> lerCsv(byte[] conteudo) {
        CSVFormat formato = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .setTrim(true)
                .build();
        List<Map<String, String>> linhas = new ArrayList<>();
        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(conteudo), StandardCharsets.UTF_8);
             CSVParser parser = CSVParser.parse(reader, formato)) {
            for (CSVRecord registro : parser) {
                Map<String, String> linha = new LinkedHashMap<>();
                registro.toMap().forEach((k, v) -> linha.put(normalizar(k), v == null ? "" : v.trim()));
                linhas.add(linha);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Falha ao ler CSV: " + e.getMessage(), e);
        }
        return linhas;
    }

    private List<Map<String, String>> lerXlsx(byte[] conteudo) {
        List<Map<String, String>> linhas = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(conteudo))) {
            Sheet sheet = wb.getSheetAt(0);
            Row cabecalho = sheet.getRow(sheet.getFirstRowNum());
            if (cabecalho == null) {
                return linhas;
            }
            List<String> colunas = new ArrayList<>();
            for (Cell c : cabecalho) {
                colunas.add(normalizar(fmt.formatCellValue(c)));
            }
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                Map<String, String> linha = new LinkedHashMap<>();
                boolean vazia = true;
                for (int col = 0; col < colunas.size(); col++) {
                    String valor = fmt.formatCellValue(row.getCell(col)).trim();
                    if (!valor.isEmpty()) {
                        vazia = false;
                    }
                    linha.put(colunas.get(col), valor);
                }
                if (!vazia) {
                    linhas.add(linha);
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Falha ao ler XLSX: " + e.getMessage(), e);
        }
        return linhas;
    }

    private static String normalizar(String chave) {
        if (chave == null) {
            return "";
        }
        // Remove o BOM (U+FEFF) que Excel/Google Sheets põem no início do CSV — sem
        // isso o 1º cabeçalho viraria "\uFEFFnome" e o "nome" obrigatório ficaria vazio.
        return chave.replace("\uFEFF", "").trim().toLowerCase().replace(' ', '_');
    }
}
