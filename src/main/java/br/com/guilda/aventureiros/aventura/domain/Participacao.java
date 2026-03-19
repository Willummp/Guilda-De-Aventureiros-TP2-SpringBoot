package br.com.guilda.aventureiros.aventura.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Representa a participação de um Aventureiro em uma Missão.
 * Chave composta: (missao_id, aventureiro_id). Garante unicidade do par.
 */
@Entity
@Table(schema = "aventura", name = "participacoes")
public class Participacao {

    @EmbeddedId
    private ParticipacaoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("missaoId")
    @JoinColumn(name = "missao_id", nullable = false)
    private Missao missao;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("aventureiroId")
    @JoinColumn(name = "aventureiro_id", nullable = false)
    private Aventureiro aventureiro;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel_missao", nullable = false)
    private PapelMissao papelMissao;

    @DecimalMin("0.0")
    @Column(name = "recompensa_ouro", nullable = false, precision = 12, scale = 2)
    private BigDecimal recompensaOuro = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean mvp = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Participacao() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public ParticipacaoId getId() { return id; }
    public void setId(ParticipacaoId id) { this.id = id; }
    public Missao getMissao() { return missao; }
    public void setMissao(Missao missao) { this.missao = missao; }
    public Aventureiro getAventureiro() { return aventureiro; }
    public void setAventureiro(Aventureiro aventureiro) { this.aventureiro = aventureiro; }
    public PapelMissao getPapelMissao() { return papelMissao; }
    public void setPapelMissao(PapelMissao papelMissao) { this.papelMissao = papelMissao; }
    public BigDecimal getRecompensaOuro() { return recompensaOuro; }
    public void setRecompensaOuro(BigDecimal recompensaOuro) { this.recompensaOuro = recompensaOuro; }
    public Boolean getMvp() { return mvp; }
    public void setMvp(Boolean mvp) { this.mvp = mvp; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
