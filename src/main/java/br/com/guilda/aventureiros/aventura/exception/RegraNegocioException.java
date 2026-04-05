package br.com.guilda.aventureiros.aventura.exception;

/**
 * Violação de regra de negócio do domínio aventura (HTTP 400).
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String message) {
        super(message);
    }
}
