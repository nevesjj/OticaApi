package api.otica.exception;

public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String recurso, Long id) {
        super(recurso + " não encontrado(a): " + id);
    }
}
