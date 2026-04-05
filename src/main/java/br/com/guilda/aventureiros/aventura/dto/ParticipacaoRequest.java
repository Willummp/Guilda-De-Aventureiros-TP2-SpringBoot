package br.com.guilda.aventureiros.aventura.dto;

import br.com.guilda.aventureiros.aventura.domain.PapelMissao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ParticipacaoRequest {

    @NotNull(message = "aventureiroId é obrigatório")
    private Long aventureiroId;

    @NotNull(message = "papel na missão é obrigatório")
    private PapelMissao papelMissao;

    @NotNull(message = "recompensa em ouro é obrigatória")
    @DecimalMin(value = "0.0", inclusive = true, message = "recompensa em ouro deve ser >= 0")
    private BigDecimal recompensaOuro;

    private Boolean mvp;

    public Long getAventureiroId() { return aventureiroId; }
    public void setAventureiroId(Long aventureiroId) { this.aventureiroId = aventureiroId; }
    public PapelMissao getPapelMissao() { return papelMissao; }
    public void setPapelMissao(PapelMissao papelMissao) { this.papelMissao = papelMissao; }
    public BigDecimal getRecompensaOuro() { return recompensaOuro; }
    public void setRecompensaOuro(BigDecimal recompensaOuro) { this.recompensaOuro = recompensaOuro; }
    public Boolean getMvp() { return mvp; }
    public void setMvp(Boolean mvp) { this.mvp = mvp; }
}
