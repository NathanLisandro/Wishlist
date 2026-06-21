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
                if (ext.matches("\\.[a-z0-9]{2,5}")) {
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
}
