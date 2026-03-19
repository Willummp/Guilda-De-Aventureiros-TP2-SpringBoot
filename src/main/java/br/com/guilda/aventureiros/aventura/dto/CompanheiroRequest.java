package br.com.guilda.aventureiros.aventura.dto;

import br.com.guilda.aventureiros.aventura.domain.Especie;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para definição ou substituição de um companheiro de aventureiro.
 */
public class CompanheiroRequest {

    /**
     * O nome do companheiro não pode ser vazio.
     */
    @NotBlank(message = "nome do companheiro é obrigatório e não pode ser vazio")
    private String nome;

    /**
     * A espécie do companheiro deve pertencer ao grupo das permitidas.
     */
    @NotNull(message = "especie inválida ou obrigatória")
    private Especie especie;

    /**
     * O nível de lealdade deve ser entre 0 e 100 obrigatoriamente.
     */
    @NotNull(message = "lealdade é obrigatória")
    @Min(value = 0, message = "lealdade deve estar entre 0 e 100")
    @Max(value = 100, message = "lealdade deve estar entre 0 e 100")
    private Integer lealdade;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Especie getEspecie() {
        return especie;
    }

    public void setEspecie(Especie especie) {
        this.especie = especie;
    }

    public Integer getLealdade() {
        return lealdade;
    }

    public void setLealdade(Integer lealdade) {
        this.lealdade = lealdade;
    }
}
