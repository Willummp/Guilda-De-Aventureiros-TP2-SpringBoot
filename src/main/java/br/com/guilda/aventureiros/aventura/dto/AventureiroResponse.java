package br.com.guilda.aventureiros.aventura.dto;

import br.com.guilda.aventureiros.aventura.domain.Aventureiro;
import br.com.guilda.aventureiros.aventura.domain.Classe;

/**
 * Representa os dados completos de um Aventureiro.
 */
public class AventureiroResponse {

    private Long id;
    private String nome;
    private Classe classe;
    private Integer nivel;
    private boolean ativo;
    private Long organizacaoId;
    private CompanheiroResponse companheiro;
    private Long totalMissoes;
    private String ultimaMissao;

    public AventureiroResponse(Aventureiro aventureiro) {
        this.id = aventureiro.getId();
        this.nome = aventureiro.getNome();
        this.classe = aventureiro.getClasse();
        this.nivel = aventureiro.getNivel();
        this.ativo = aventureiro.isAtivo();
        this.organizacaoId = aventureiro.getOrganizacao() != null ? aventureiro.getOrganizacao().getId() : null;
        if (aventureiro.getCompanheiro() != null) {
            this.companheiro = new CompanheiroResponse(
                    aventureiro.getCompanheiro().getNome(),
                    aventureiro.getCompanheiro().getEspecie(),
                    aventureiro.getCompanheiro().getLealdade()
            );
        }
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Classe getClasse() { return classe; }
    public Integer getNivel() { return nivel; }
    public boolean isAtivo() { return ativo; }
    public Long getOrganizacaoId() { return organizacaoId; }
    public CompanheiroResponse getCompanheiro() { return companheiro; }
    
    public Long getTotalMissoes() { return totalMissoes; }
    public void setTotalMissoes(Long totalMissoes) { this.totalMissoes = totalMissoes; }
    public String getUltimaMissao() { return ultimaMissao; }
    public void setUltimaMissao(String ultimaMissao) { this.ultimaMissao = ultimaMissao; }
}
