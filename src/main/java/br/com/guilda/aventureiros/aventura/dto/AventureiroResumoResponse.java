package br.com.guilda.aventureiros.aventura.dto;

import br.com.guilda.aventureiros.aventura.domain.Aventureiro;
import br.com.guilda.aventureiros.aventura.domain.Classe;

/**
 * Representa um resumo dos dados de um Aventureiro, usado principalmente em listagens.
 * A principal diferença aqui é que não há inclusão de detalhes do companheiro.
 */
public class AventureiroResumoResponse {

    private Long id;
    private String nome;
    private Classe classe;
    private Integer nivel;
    private boolean ativo;

    public AventureiroResumoResponse(Aventureiro aventureiro) {
        this.id = aventureiro.getId();
        this.nome = aventureiro.getNome();
        this.classe = aventureiro.getClasse();
        this.nivel = aventureiro.getNivel();
        this.ativo = aventureiro.isAtivo();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Classe getClasse() {
        return classe;
    }

    public Integer getNivel() {
        return nivel;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
