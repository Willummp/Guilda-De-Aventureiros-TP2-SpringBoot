package br.com.guilda.aventureiros.aventura.service;

import br.com.guilda.aventureiros.aventura.domain.Aventureiro;
import br.com.guilda.aventureiros.aventura.domain.Missao;
import br.com.guilda.aventureiros.aventura.domain.Participacao;
import br.com.guilda.aventureiros.aventura.domain.ParticipacaoId;
import br.com.guilda.aventureiros.aventura.domain.StatusMissao;
import br.com.guilda.aventureiros.aventura.dto.ParticipacaoRequest;
import br.com.guilda.aventureiros.aventura.exception.RecursoNaoEncontradoException;
import br.com.guilda.aventureiros.aventura.exception.RegraNegocioException;
import br.com.guilda.aventureiros.aventura.repository.AventureiroRepository;
import br.com.guilda.aventureiros.aventura.repository.MissaoRepository;
import br.com.guilda.aventureiros.aventura.repository.ParticipacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

@Service
public class ParticipacaoService {

    private static final EnumSet<StatusMissao> STATUS_ACEITA_NOVO_PARTICIPANTE =
            EnumSet.of(StatusMissao.PLANEJADA, StatusMissao.EM_ANDAMENTO);

    private final MissaoRepository missaoRepository;
    private final AventureiroRepository aventureiroRepository;
    private final ParticipacaoRepository participacaoRepository;

    public ParticipacaoService(MissaoRepository missaoRepository,
                               AventureiroRepository aventureiroRepository,
                               ParticipacaoRepository participacaoRepository) {
        this.missaoRepository = missaoRepository;
        this.aventureiroRepository = aventureiroRepository;
        this.participacaoRepository = participacaoRepository;
    }

    @Transactional
    public Participacao registrar(Long missaoId, ParticipacaoRequest request) {
        Missao missao = missaoRepository.findById(missaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("missão não encontrada"));
        Aventureiro aventureiro = aventureiroRepository.findById(request.getAventureiroId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("aventureiro não encontrado"));

        if (!missao.getOrganizacao().getId().equals(aventureiro.getOrganizacao().getId())) {
            throw new RegraNegocioException("missão e aventureiro devem pertencer à mesma organização");
        }
        if (!aventureiro.isAtivo()) {
            throw new RegraNegocioException("aventureiro inativo não pode ser associado a novas missões");
        }
        if (!STATUS_ACEITA_NOVO_PARTICIPANTE.contains(missao.getStatus())) {
            throw new RegraNegocioException(
                    "missão não aceita novos participantes no status atual: " + missao.getStatus());
        }

        ParticipacaoId id = new ParticipacaoId(missaoId, aventureiro.getId());
        if (participacaoRepository.existsById(id)) {
            throw new RegraNegocioException("participação já existe para este par (missão, aventureiro)");
        }

        Participacao p = new Participacao();
        p.setId(id);
        p.setMissao(missao);
        p.setAventureiro(aventureiro);
        p.setPapelMissao(request.getPapelMissao());
        p.setRecompensaOuro(request.getRecompensaOuro());
        p.setMvp(Boolean.TRUE.equals(request.getMvp()));

        return participacaoRepository.save(p);
    }
}
