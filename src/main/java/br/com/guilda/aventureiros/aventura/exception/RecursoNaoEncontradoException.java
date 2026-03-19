package br.com.guilda.aventureiros.aventura.exception;

/**
 * Exceção lançada quando um recurso (Aventureiro, Companheiro, etc) não é encontrado no sistema.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
