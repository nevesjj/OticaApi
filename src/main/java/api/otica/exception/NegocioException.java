package api.otica.exception;

/** Violação de regra de negócio (retorna HTTP 422). */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
