package br.com.guilda.aventureiros.aventura.dto;

import br.com.guilda.aventureiros.aventura.domain.Missao;
import br.com.guilda.aventureiros.aventura.domain.NivelPerigo;
import br.com.guilda.aventureiros.aventura.domain.StatusMissao;

import java.time.OffsetDateTime;

public class MissaoResumoResponse {

    private Long id;
    private Long organizacaoId;
    private String titulo;
    private NivelPerigo nivelPerigo;
    private StatusMissao status;
    private OffsetDateTime createdAt;
    private OffsetDateTime dataInicio;
    private OffsetDateTime dataTermino;

    public MissaoResumoResponse(Missao m) {
        this.id = m.getId();
        this.organizacaoId = m.getOrganizacao() != null ? m.getOrganizacao().getId() : null;
        this.titulo = m.getTitulo();
        this.nivelPerigo = m.getNivelPerigo();
        this.status = m.getStatus();
        this.createdAt = m.getCreatedAt();
        this.dataInicio = m.getDataInicio();
        this.dataTermino = m.getDataTermino();
    }

    public Long getId() { return id; }
    public Long getOrganizacaoId() { return organizacaoId; }
    public String getTitulo() { return titulo; }
    public NivelPerigo getNivelPerigo() { return nivelPerigo; }
    public StatusMissao getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getDataInicio() { return dataInicio; }
    public OffsetDateTime getDataTermino() { return dataTermino; }
}
