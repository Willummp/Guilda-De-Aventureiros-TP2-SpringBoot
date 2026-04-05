package br.com.guilda.aventureiros;

import br.com.guilda.aventureiros.audit.domain.Organizacao;
import br.com.guilda.aventureiros.audit.domain.Usuario;
import br.com.guilda.aventureiros.audit.repository.OrganizacaoRepository;
import br.com.guilda.aventureiros.audit.repository.UsuarioRepository;
import br.com.guilda.aventureiros.aventura.domain.*;
import br.com.guilda.aventureiros.aventura.dto.ParticipacaoRequest;
import br.com.guilda.aventureiros.aventura.exception.RegraNegocioException;
import br.com.guilda.aventureiros.aventura.repository.AventureiroRepository;
import br.com.guilda.aventureiros.aventura.repository.MissaoRepository;
import br.com.guilda.aventureiros.aventura.repository.ParticipacaoRepository;
import br.com.guilda.aventureiros.aventura.service.ParticipacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Testes de integração para as consultas do domínio aventura (Partes 2 e 3).
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=update"
})
@Transactional
class AventuraIntegracaoTest {

    @Autowired private AventureiroRepository aventureiroRepository;
    @Autowired private MissaoRepository missaoRepository;
    @Autowired private ParticipacaoRepository participacaoRepository;
    @Autowired private OrganizacaoRepository organizacaoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ParticipacaoService participacaoService;

    private Organizacao org;
    private Usuario usuario;
    private Aventureiro aventureiro;
    private Missao missao;

    @BeforeEach
    void setup() {
        List<Organizacao> orgs = organizacaoRepository.findAll();
        assertThat(orgs).isNotEmpty();
        org = orgs.get(0);

        List<Usuario> usuarios = usuarioRepository.findAll();
        assertThat(usuarios).isNotEmpty();
        usuario = usuarios.get(0);

        aventureiro = new Aventureiro("Gandalf Teste", Classe.MAGO, 10);
        aventureiro.setOrganizacao(org);
        aventureiro.setUsuarioResponsavel(usuario);
        aventureiro = aventureiroRepository.save(aventureiro);

        missao = new Missao();
        missao.setOrganizacao(org);
        missao.setTitulo("Missão Teste TP2");
        missao.setNivelPerigo(NivelPerigo.MEDIO);
        missao = missaoRepository.save(missao);
    }

    @Test
    void deveListarAventureirosPorClasse() {
        Page<Aventureiro> resultado = aventureiroRepository.findByFiltros(
                Classe.MAGO, null, null, PageRequest.of(0, 10));
        assertThat(resultado.getContent()).isNotEmpty();
        assertThat(resultado.getContent()).allMatch(a -> a.getClasse() == Classe.MAGO);
        System.out.println("=== Aventureiros MAGO: " + resultado.getTotalElements() + " ===");
    }

    @Test
    void deveListarAventureirosPorStatusAtivo() {
        Page<Aventureiro> resultado = aventureiroRepository.findByFiltros(
                null, true, null, PageRequest.of(0, 10));
        assertThat(resultado.getContent()).allMatch(Aventureiro::isAtivo);
        System.out.println("=== Aventureiros ativos: " + resultado.getTotalElements() + " ===");
    }

    @Test
    void deveListarAventureirosPorNivelMinimo() {
        Page<Aventureiro> resultado = aventureiroRepository.findByFiltros(
                null, null, 5, PageRequest.of(0, 10));
        assertThat(resultado.getContent()).allMatch(a -> a.getNivel() >= 5);
        System.out.println("=== Aventureiros nível >= 5: " + resultado.getTotalElements() + " ===");
    }

    @Test
    void deveBuscarAventureirosPorNomeParcial() {
        Page<Aventureiro> resultado = aventureiroRepository.findByNomeContainingIgnoreCase(
                "gandalf", PageRequest.of(0, 10, Sort.by("nome")));
        assertThat(resultado.getContent()).isNotEmpty();
        System.out.println("=== Aventureiros com 'gandalf': " + resultado.getTotalElements() + " ===");
    }

    @Test
    void deveBuscarAventureirosPorNomeOrdenadoPorNivelDesc() {
        Page<Aventureiro> resultado = aventureiroRepository.findByNomeContainingIgnoreCase(
                "gandalf", PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "nivel")));
        assertThat(resultado.getContent()).isNotEmpty();
        System.out.println("=== Busca 'gandalf' ordenada por nível DESC ===");
    }

    @Test
    void deveCarregarAventureiroComCompanheiro() {
        Companheiro comp = new Companheiro("Fiel", Especie.LOBO, 80);
        comp.setAventureiro(aventureiro);
        aventureiro.setCompanheiro(comp);
        aventureiroRepository.save(aventureiro);
        aventureiroRepository.flush();

        Optional<Aventureiro> resultado = aventureiroRepository.findByIdWithCompanheiro(aventureiro.getId());
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCompanheiro()).isNotNull();
        assertThat(resultado.get().getCompanheiro().getNome()).isEqualTo("Fiel");
        System.out.println("=== Companheiro carregado: " + resultado.get().getCompanheiro().getNome() + " ===");
    }

    @Test
    void deveListarMissoesPorStatus() {
        Page<Missao> resultado = missaoRepository.findByFiltrosSemPeriodo(
                StatusMissao.PLANEJADA, null, PageRequest.of(0, 10));
        assertThat(resultado.getContent()).isNotEmpty();
        assertThat(resultado.getContent()).allMatch(m -> m.getStatus() == StatusMissao.PLANEJADA);
        System.out.println("=== Missões PLANEJADA: " + resultado.getTotalElements() + " ===");
    }

    @Test
    void deveListarMissoesPorNivelPerigo() {
        Page<Missao> resultado = missaoRepository.findByFiltrosSemPeriodo(
                null, NivelPerigo.MEDIO, PageRequest.of(0, 10));
        assertThat(resultado.getContent()).allMatch(m -> m.getNivelPerigo() == NivelPerigo.MEDIO);
        System.out.println("=== Missões MÉDIO: " + resultado.getTotalElements() + " ===");
    }

    @Test
    void deveListarMissoesPorIntervaloSobrepostoADataInicioETermino() {
        OffsetDateTime inicio = OffsetDateTime.now().plusDays(2);
        OffsetDateTime fim = OffsetDateTime.now().plusDays(8);
        Missao mAgendada = new Missao();
        mAgendada.setOrganizacao(org);
        mAgendada.setTitulo("Missão com janela temporal");
        mAgendada.setNivelPerigo(NivelPerigo.BAIXO);
        mAgendada.setDataInicio(inicio);
        mAgendada.setDataTermino(fim);
        missaoRepository.save(mAgendada);
        missaoRepository.flush();

        OffsetDateTime periodoIni = OffsetDateTime.now();
        OffsetDateTime periodoFim = OffsetDateTime.now().plusDays(30);
        Page<Missao> page = missaoRepository.findByFiltrosComPeriodo(
                null, null, periodoIni, periodoFim, PageRequest.of(0, 50));
        assertThat(page.getContent().stream().anyMatch(m -> "Missão com janela temporal".equals(m.getTitulo())))
                .isTrue();
        System.out.println("=== Filtro por intervalo (sobreposição) inclui missão agendada ===");
    }

    @Test
    void deveRegistrarParticipacaoEmMissaoViaServicoEDetalharParticipantes() {
        ParticipacaoRequest req = new ParticipacaoRequest();
        req.setAventureiroId(aventureiro.getId());
        req.setPapelMissao(PapelMissao.LIDER);
        req.setRecompensaOuro(new BigDecimal("250.00"));
        req.setMvp(true);
        participacaoService.registrar(missao.getId(), req);

        List<Participacao> participacoes = participacaoRepository.findByMissaoId(missao.getId());
        assertThat(participacoes).hasSize(1);
        assertThat(participacoes.get(0).getPapelMissao()).isEqualTo(PapelMissao.LIDER);
        assertThat(participacoes.get(0).getAventureiro().getNome()).containsIgnoringCase("Gandalf");
        System.out.println("=== Participação + detalhe (lista por missão) ===");
    }

    @Test
    void deveRejeitarParticipacaoQuandoAventureiroInativo() {
        aventureiro.setAtivo(false);
        aventureiroRepository.save(aventureiro);

        ParticipacaoRequest req = new ParticipacaoRequest();
        req.setAventureiroId(aventureiro.getId());
        req.setPapelMissao(PapelMissao.COMBATENTE);
        req.setRecompensaOuro(BigDecimal.TEN);

        assertThrows(RegraNegocioException.class, () -> participacaoService.registrar(missao.getId(), req));
    }

    @Test
    void deveRejeitarParticipacaoQuandoMissaoNaoAceitaNovosParticipantes() {
        missao.setStatus(StatusMissao.CONCLUIDA);
        missaoRepository.save(missao);

        ParticipacaoRequest req = new ParticipacaoRequest();
        req.setAventureiroId(aventureiro.getId());
        req.setPapelMissao(PapelMissao.COMBATENTE);
        req.setRecompensaOuro(BigDecimal.TEN);

        assertThrows(RegraNegocioException.class, () -> participacaoService.registrar(missao.getId(), req));
    }

    @Test
    void deveRejeitarParticipacaoQuandoOrganizacoesDiferentes() {
        List<Organizacao> orgs = organizacaoRepository.findAll();
        assumeTrue(orgs.size() >= 2, "banco legado precisa de pelo menos 2 organizações");
        Organizacao outra = orgs.stream().filter(o -> !o.getId().equals(org.getId())).findFirst().orElseThrow();
        Missao missaoOutraOrg = new Missao();
        missaoOutraOrg.setOrganizacao(outra);
        missaoOutraOrg.setTitulo("Missão outra guilda");
        missaoOutraOrg.setNivelPerigo(NivelPerigo.ALTO);
        final Long missaoOutraId = missaoRepository.save(missaoOutraOrg).getId();

        ParticipacaoRequest req = new ParticipacaoRequest();
        req.setAventureiroId(aventureiro.getId());
        req.setPapelMissao(PapelMissao.SUPORTE);
        req.setRecompensaOuro(BigDecimal.ONE);

        assertThrows(RegraNegocioException.class, () -> participacaoService.registrar(missaoOutraId, req));
    }

    @Test
    void deveGerarRankingDeParticipacao() {
        ParticipacaoRequest req = new ParticipacaoRequest();
        req.setAventureiroId(aventureiro.getId());
        req.setPapelMissao(PapelMissao.COMBATENTE);
        req.setRecompensaOuro(new BigDecimal("100.00"));
        participacaoService.registrar(missao.getId(), req);
        participacaoRepository.flush();

        List<Object[]> ranking = participacaoRepository.findRankingParticipacao(
                OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1));
        assertThat(ranking).isNotEmpty();
        System.out.println("=== Ranking: " + ranking.size() + " aventureiros ===");
    }

    @Test
    void deveGerarRelatorioMissoes() {
        ParticipacaoRequest req = new ParticipacaoRequest();
        req.setAventureiroId(aventureiro.getId());
        req.setPapelMissao(PapelMissao.SUPORTE);
        req.setRecompensaOuro(new BigDecimal("50.00"));
        participacaoService.registrar(missao.getId(), req);
        participacaoRepository.flush();

        List<Object[]> relatorio = participacaoRepository.findRelatorioMissoes();
        assertThat(relatorio).isNotEmpty();
        System.out.println("=== Missões no relatório: " + relatorio.size() + " ===");
    }
}
