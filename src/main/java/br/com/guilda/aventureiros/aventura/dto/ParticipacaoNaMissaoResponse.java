package br.com.guilda.aventureiros.aventura.dto;

import br.com.guilda.aventureiros.aventura.domain.Participacao;
import br.com.guilda.aventureiros.aventura.domain.PapelMissao;

import java.math.BigDecimal;

public class ParticipacaoNaMissaoResponse {

    private Long aventureiroId;
    private String aventureiroNome;
    private PapelMissao papelMissao;
    private BigDecimal recompensaOuro;
    private boolean mvp;

    public ParticipacaoNaMissaoResponse(Participacao p) {
        this.aventureiroId = p.getAventureiro().getId();
        this.aventureiroNome = p.getAventureiro().getNome();
        this.papelMissao = p.getPapelMissao();
        this.recompensaOuro = p.getRecompensaOuro();
        this.mvp = Boolean.TRUE.equals(p.getMvp());
    }

    public Long getAventureiroId() { return aventureiroId; }
    public String getAventureiroNome() { return aventureiroNome; }
    public PapelMissao getPapelMissao() { return papelMissao; }
    public BigDecimal getRecompensaOuro() { return recompensaOuro; }
    public boolean isMvp() { return mvp; }
}
