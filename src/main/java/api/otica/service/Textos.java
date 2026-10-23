package api.otica.service;

/** Utilitários de texto para entrada de dados e buscas. */
final class Textos {

    private Textos() {
    }

    /** Converte o termo digitado no padrão LIKE minúsculo (ou "%" para tudo). */
    static String termoBusca(String q) {
        return q == null || q.isBlank() ? "%" : "%" + q.trim().toLowerCase() + "%";
    }

    /** String vazia vira null — evita violar colunas únicas (CPF, CNPJ) com "". */
    static String limpar(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
