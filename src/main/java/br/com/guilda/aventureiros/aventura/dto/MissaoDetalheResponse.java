package br.com.guilda.aventureiros.aventura.dto;

import br.com.guilda.aventureiros.aventura.domain.Missao;

import java.util.List;

public class MissaoDetalheResponse extends MissaoResumoResponse {

    private List<ParticipacaoNaMissaoResponse> participantes;

    public MissaoDetalheResponse(Missao m, List<ParticipacaoNaMissaoResponse> participantes) {
        super(m);
        this.participantes = participantes;
    }

    public List<ParticipacaoNaMissaoResponse> getParticipantes() { return participantes; }
}
