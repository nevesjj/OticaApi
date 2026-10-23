package api.otica.domain;

public enum FormaPagamento {
    DINHEIRO, PIX, CARTAO_DEBITO, CARTAO_CREDITO, CREDIARIO, BOLETO;

    /** Formas recebidas no ato da venda (não geram parcelas a receber). */
    public boolean isAVista() {
        return this == DINHEIRO || this == PIX || this == CARTAO_DEBITO;
    }
}
