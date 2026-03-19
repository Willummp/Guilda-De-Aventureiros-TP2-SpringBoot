package br.com.guilda.aventureiros.aventura.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Representa um Companheiro vinculado a um Aventureiro.
 * Não pode existir isoladamente. Remoção em cascata via Aventureiro.
 */
@Entity
@Table(schema = "aventura", name = "companheiros")
public class Companheiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aventureiro_id", nullable = false, unique = true)
    private Aventureiro aventureiro;

    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Especie especie;

    @Min(0)
    @Max(100)
    @Column(nullable = false)
    private Integer lealdade;

    public Companheiro() {}

    public Companheiro(String nome, Especie especie, Integer lealdade) {
        this.nome = nome;
        this.especie = especie;
        this.lealdade = lealdade;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Aventureiro getAventureiro() { return aventureiro; }
    public void setAventureiro(Aventureiro aventureiro) { this.aventureiro = aventureiro; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Especie getEspecie() { return especie; }
    public void setEspecie(Especie especie) { this.especie = especie; }
    public Integer getLealdade() { return lealdade; }
    public void setLealdade(Integer lealdade) { this.lealdade = lealdade; }
}
