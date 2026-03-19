package br.com.guilda.aventureiros.aventura.exception;

import java.util.List;

/**
 * Representa o corpo da resposta padrão exigido no formato JSON em caso de
 * erro.
 *
 * <pre>
 * {
 *   "mensagem": "Solicitação inválida",
 *   "detalhes": [
 *     "classe inválida",
 *     "nivel deve ser maior ou igual a 1"
 *   ]
 * }
 * </pre>
 */
public class ErroPadraoResponse {

    private String mensagem;
    private List<String> detalhes;

    public ErroPadraoResponse(String mensagem, List<String> detalhes) {
        this.mensagem = mensagem;
        this.detalhes = detalhes;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public List<String> getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(List<String> detalhes) {
        this.detalhes = detalhes;
    }
}
