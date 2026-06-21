package com.wishlist.nathan.application.port;

/**
 * Armazenamento de arquivos binários (fotos do evento). Abstrai o destino físico
 * (disco local em dev, S3/object storage em prod) — a aplicação só conhece o
 * caminho público resultante. Implementação atual: {@code ArmazenamentoLocal}.
 */
public interface ArmazenamentoPort {

    /**
     * Grava o conteúdo sob a "pasta" lógica informada e devolve o caminho público
     * (ex.: {@code /media/eventos/<id>/<arquivo>}) para guardar no banco/servir ao
     * navegador.
     *
     * @param pasta        agrupamento lógico (ex.: {@code "eventos/" + eventoId})
     * @param nomeOriginal nome do arquivo enviado (usado p/ inferir a extensão)
     * @param contentType  MIME informado pelo upload (validado: só imagens)
     * @param conteudo     bytes do arquivo
     */
    String salvar(String pasta, String nomeOriginal, String contentType, byte[] conteudo);

    /** Apaga um arquivo pelo caminho público antes salvo (no-op se já não existir). */
    void apagar(String caminhoPublico);
}
