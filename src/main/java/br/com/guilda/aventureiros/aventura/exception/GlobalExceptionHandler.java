package br.com.guilda.aventureiros.aventura.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Interceptador global de exceções para formatar as respostas de erro da API.
 * Garante que a estrutura exigida pelo Conselho da Guilda seja sempre respeitada.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Tratamento de erros de validação (ex: campos em branco, números menores que o permitido).
     * Disparado quando objetos anotados com {@code @Valid} falham na resolução no Controller.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroPadraoResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> detalhes = new ArrayList<>();
        
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            detalhes.add(error.getDefaultMessage());
        }

        ErroPadraoResponse erro = new ErroPadraoResponse("Solicitação inválida", detalhes);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    /**
     * Tratamento de falhas de leitura do JSON (ex: valores de Enum inválidos enviados pelo cliente).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroPadraoResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        List<String> detalhes = new ArrayList<>();
        
        // Mensagem genérica se a desserialização do JSON falhar (ex: Classe enviada que não existe no Enum)
        detalhes.add("Formato de requisição inválido. Verifique os valores enviados, como 'classe' ou 'especie'.");
        
        ErroPadraoResponse erro = new ErroPadraoResponse("Solicitação inválida", detalhes);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    /**
     * Tratamento proativo para recursos não encontrados no banco de dados (ArrayList).
     */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroPadraoResponse> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        List<String> detalhes = new ArrayList<>();
        detalhes.add(ex.getMessage());

        ErroPadraoResponse erro = new ErroPadraoResponse("recurso não encontrado", detalhes);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    /**
     * Tratamento de fallback para qualquer outro erro não mapeado explicitamente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroPadraoResponse> handleAllExceptions(Exception ex) {
        List<String> detalhes = new ArrayList<>();
        detalhes.add(ex.getMessage());
        
        ErroPadraoResponse erro = new ErroPadraoResponse("Erro interno no servidor da Guilda", detalhes);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}
