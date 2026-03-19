package br.com.guilda.aventureiros.aventura.dto;

import br.com.guilda.aventureiros.aventura.domain.Especie;

public class CompanheiroResponse {
    private String nome;
    private Especie especie;
    private Integer lealdade;

    public CompanheiroResponse() {}

    public CompanheiroResponse(String nome, Especie especie, Integer lealdade) {
        this.nome = nome;
        this.especie = especie;
        this.lealdade = lealdade;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Especie getEspecie() { return especie; }
    public void setEspecie(Especie especie) { this.especie = especie; }
    public Integer getLealdade() { return lealdade; }
    public void setLealdade(Integer lealdade) { this.lealdade = lealdade; }
}
