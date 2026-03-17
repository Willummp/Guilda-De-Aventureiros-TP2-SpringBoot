package br.com.guilda.aventureiros.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Chave composta para a entidade Participacao (missao_id + aventureiro_id).
 */
@Embeddable
public class ParticipacaoId implements Serializable {

    @Column(name = "missao_id")
    private Long missaoId;

    @Column(name = "aventureiro_id")
    private Long aventureiroId;

    public ParticipacaoId() {}

    public ParticipacaoId(Long missaoId, Long aventureiroId) {
        this.missaoId = missaoId;
        this.aventureiroId = aventureiroId;
    }

    public Long getMissaoId() { return missaoId; }
    public void setMissaoId(Long missaoId) { this.missaoId = missaoId; }
    public Long getAventureiroId() { return aventureiroId; }
    public void setAventureiroId(Long aventureiroId) { this.aventureiroId = aventureiroId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipacaoId)) return false;
        ParticipacaoId that = (ParticipacaoId) o;
        return Objects.equals(missaoId, that.missaoId) && Objects.equals(aventureiroId, that.aventureiroId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(missaoId, aventureiroId);
    }
}
