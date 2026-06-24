package com.wishlist.nathan.infrastructure.ai;

import com.wishlist.nathan.application.port.BuscadorHtmlPort;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Busca o texto de uma URL pública para alimentar a extração por IA. Protege contra
 * SSRF: só http/https, bloqueia IP privado/loopback/link-local/metadata, não segue
 * redirecionamentos (evita rebind para host interno), e limita tempo e tamanho.
 */
@Component
public class BuscadorHtmlHttp implements BuscadorHtmlPort {

    private static final int MAX_CHARS = 200_000;
    private static final String UA = "Mozilla/5.0 (compatible; ListaPresentesBot/1.0)";

    @Override
    public String buscarTexto(String url) {
        URI uri = validar(url);
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(8))
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();
            HttpRequest req = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(12))
                    .header("User-Agent", UA)
                    .header("Accept", "text/html,application/xhtml+xml")
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            int sc = resp.statusCode();
            if (sc >= 300 && sc < 400) {
                throw new IllegalArgumentException("Não foi possível ler o link (redirecionamento). "
                        + "Cole a URL final do produto/lista.");
            }
            if (sc >= 400) {
                throw new IllegalArgumentException("Não foi possível abrir o link (HTTP " + sc + ").");
            }
            String texto = stripHtml(resp.body());
            if (texto.length() > MAX_CHARS) {
                texto = texto.substring(0, MAX_CHARS);
            }
            if (texto.isBlank()) {
                throw new IllegalArgumentException("O link não retornou texto utilizável.");
            }
            return texto;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Falha ao ler o link: " + e.getMessage());
        }
    }

    private static URI validar(String url) {
        URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("URL inválida.");
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new IllegalArgumentException("Use um link http(s) válido.");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("URL sem host.");
        }
        try {
            for (InetAddress addr : InetAddress.getAllByName(host)) {
                if (addr.isLoopbackAddress() || addr.isAnyLocalAddress()
                        || addr.isSiteLocalAddress() || addr.isLinkLocalAddress()
                        || addr.isMulticastAddress()) {
                    throw new IllegalArgumentException("Endereço de rede não permitido.");
                }
            }
        } catch (java.net.UnknownHostException e) {
            throw new IllegalArgumentException("Host não encontrado.");
        }
        return uri;
    }

    /** Remove script/style e tags, decodifica entidades comuns e colapsa espaços. */
    private static String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        String semScript = html.replaceAll("(?is)<(script|style|noscript)[^>]*>.*?</\\1>", " ");
        String semTags = semScript.replaceAll("(?s)<[^>]+>", " ");
        String decod = semTags
                .replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<")
                .replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'");
        return decod.replaceAll("\\s+", " ").trim();
    }
}
