package com.wishlist.nathan.infrastructure.file;

import com.wishlist.nathan.application.port.ArmazenamentoPort;
import com.wishlist.nathan.infrastructure.config.AppProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

/**
 * Armazenamento em disco local. Grava sob {@code app.storage.dir} e devolve um
 * caminho público {@code app.storage.public-base + "/" + pasta + "/" + arquivo},
 * servido estaticamente por {@code RecursosWebConfig}.
 *
 * <p>Em produção, troque por um adapter de object storage (S3) sem tocar no
 * domínio nem nos casos de uso — só este componente conhece o disco.
 */
@Component
public class ArmazenamentoLocal implements ArmazenamentoPort {

    /** Imagens aceitas (defesa simples contra upload de executáveis). */
    private static final Set<String> TIPOS_PERMITIDOS =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    /** Extensões permitidas no nome gravado (whitelist — nada de .phtml etc.). */
    private static final Set<String> EXTENSOES_PERMITIDAS =
            Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    private final Path raiz;
    private final String basePublica;

    public ArmazenamentoLocal(AppProperties props) {
        this.raiz = Path.of(props.storage().dir()).toAbsolutePath().normalize();
        this.basePublica = props.storage().publicBase().replaceAll("/+$", "");
        try {
            Files.createDirectories(raiz);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar o diretório de uploads: " + raiz, e);
        }
    }

    @Override
    public String salvar(String pasta, String nomeOriginal, String contentType, byte[] conteudo) {
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Formato de imagem não suportado. Use JPG, PNG, WEBP ou GIF.");
        }
        if (conteudo == null || conteudo.length == 0) {
            throw new IllegalArgumentException("Arquivo vazio.");
        }
        // Content-Type é spoofável; confirmamos pelos magic bytes que é mesmo imagem.
        if (!pareceImagem(conteudo)) {
            throw new IllegalArgumentException("O arquivo enviado não é uma imagem válida.");
        }
        String extensao = extensaoDe(nomeOriginal, contentType);
        String arquivo = UUID.randomUUID() + extensao;
        Path pastaDestino = resolverSeguro(pasta);
        try {
            Files.createDirectories(pastaDestino);
            Files.write(pastaDestino.resolve(arquivo), conteudo);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar a imagem", e);
        }
        return basePublica + "/" + pasta + "/" + arquivo;
    }

    @Override
    public void apagar(String caminhoPublico) {
        if (caminhoPublico == null || !caminhoPublico.startsWith(basePublica + "/")) {
            return;
        }
        String relativo = caminhoPublico.substring((basePublica + "/").length());
        try {
            Files.deleteIfExists(resolverSeguro(relativo));
        } catch (IOException e) {
            // Apagar é best-effort: um arquivo órfão não deve quebrar a operação.
            throw new UncheckedIOException("Falha ao apagar a imagem", e);
        }
    }

    /** Resolve dentro da raiz e impede path traversal (".." escapando do diretório). */
    private Path resolverSeguro(String relativo) {
        Path destino = raiz.resolve(relativo).normalize();
        if (!destino.startsWith(raiz)) {
            throw new IllegalArgumentException("Caminho inválido");
        }
        return destino;
    }

    private static String extensaoDe(String nomeOriginal, String contentType) {
        if (nomeOriginal != null) {
            int ponto = nomeOriginal.lastIndexOf('.');
            if (ponto >= 0 && ponto < nomeOriginal.length() - 1) {
                String ext = nomeOriginal.substring(ponto).toLowerCase();
                if (EXTENSOES_PERMITIDAS.contains(ext)) {
                    return ext;
                }
            }
        }
        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }

    /** Confere os magic bytes do conteúdo (JPEG/PNG/GIF/WEBP). */
    private static boolean pareceImagem(byte[] b) {
        if (b.length < 12) return false;
        // JPEG: FF D8 FF
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return true;
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return true;
        // GIF: "GIF8"
        if (b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8') return true;
        // WEBP: "RIFF" .... "WEBP"
        if (b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return true;
        return false;
    }
}
