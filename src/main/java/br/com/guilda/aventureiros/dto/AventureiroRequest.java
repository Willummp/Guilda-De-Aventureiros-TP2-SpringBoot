package br.com.guilda.aventureiros.dto;

import br.com.guilda.aventureiros.domain.Classe;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Dados para registrar ou atualizar um Aventureiro.
 */
public class AventureiroRequest {

    @NotBlank(message = "nome do aventureiro é obrigatório e não pode ser vazio")
    private String nome;

    @NotNull(message = "classe inválida ou obrigatória")
    private Classe classe;

    @NotNull(message = "nível é obrigatório")
    @Min(value = 1, message = "nivel deve ser maior ou igual a 1")
    private Integer nivel;

    @NotNull(message = "organizacaoId é obrigatório")
    private Long organizacaoId;

    @NotNull(message = "usuarioResponsavelId é obrigatório")
    private Long usuarioResponsavelId;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Classe getClasse() { return classe; }
    public void setClasse(Classe classe) { this.classe = classe; }
    public Integer getNivel() { return nivel; }
    public void setNivel(Integer nivel) { this.nivel = nivel; }
    public Long getOrganizacaoId() { return organizacaoId; }
    public void setOrganizacaoId(Long organizacaoId) { this.organizacaoId = organizacaoId; }
    public Long getUsuarioResponsavelId() { return usuarioResponsavelId; }
    public void setUsuarioResponsavelId(Long usuarioResponsavelId) { this.usuarioResponsavelId = usuarioResponsavelId; }
}
