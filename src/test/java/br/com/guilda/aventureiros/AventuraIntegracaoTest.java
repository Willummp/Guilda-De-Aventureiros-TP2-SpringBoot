package br.com.guilda.aventureiros;

import br.com.guilda.aventureiros.audit.domain.Organizacao;
import br.com.guilda.aventureiros.audit.domain.Usuario;
import br.com.guilda.aventureiros.audit.repository.OrganizacaoRepository;
import br.com.guilda.aventureiros.audit.repository.UsuarioRepository;
import br.com.guilda.aventureiros.domain.*;
import br.com.guilda.aventureiros.repository.AventureiroRepository;
import br.com.guilda.aventureiros.repository.MissaoRepository;
import br.com.guilda.aventureiros.repository.ParticipacaoRepository;
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

    private Organizacao org;
    private Usuario usuario;
    private Aventureiro aventureiro;
    private Missao missao;

    @BeforeEach
    void setup() {
        // Usa dados do banco legado (audit) para criar contexto
        List<Organizacao> orgs = organizacaoRepository.findAll();
        assertThat(orgs).isNotEmpty();
        org = orgs.get(0);

        List<Usuario> usuarios = usuarioRepository.findAll();
        assertThat(usuarios).isNotEmpty();
        usuario = usuarios.get(0);

        // Cria aventureiro de teste
        aventureiro = new Aventureiro("Gandalf Teste", Classe.MAGO, 10);
        aventureiro.setOrganizacao(org);
        aventureiro.setUsuarioResponsavel(usuario);
        aventureiro = aventureiroRepository.save(aventureiro);

        // Cria missão de teste
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
    void deveCarregarAventureiroComCompanheiro() {
        // Adiciona companheiro
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
        Page<Missao> resultado = missaoRepository.findByFiltros(
                StatusMissao.PLANEJADA, null, null, null, PageRequest.of(0, 10));
        assertThat(resultado.getContent()).isNotEmpty();
        assertThat(resultado.getContent()).allMatch(m -> m.getStatus() == StatusMissao.PLANEJADA);
        System.out.println("=== Missões PLANEJADA: " + resultado.getTotalElements() + " ===");
    }

    @Test
    void deveListarMissoesPorNivelPerigo() {
        Page<Missao> resultado = missaoRepository.findByFiltros(
                null, NivelPerigo.MEDIO, null, null, PageRequest.of(0, 10));
        assertThat(resultado.getContent()).allMatch(m -> m.getNivelPerigo() == NivelPerigo.MEDIO);
        System.out.println("=== Missões MÉDIO: " + resultado.getTotalElements() + " ===");
    }

    @Test
    void deveRegistrarParticipacaoEmMissao() {
        ParticipacaoId pid = new ParticipacaoId(missao.getId(), aventureiro.getId());
        Participacao p = new Participacao();
        p.setId(pid);
        p.setMissao(missao);
        p.setAventureiro(aventureiro);
        p.setPapelMissao(PapelMissao.LIDER);
        p.setRecompensaOuro(new BigDecimal("250.00"));
        p.setMvp(true);
        participacaoRepository.save(p);

        List<Participacao> participacoes = participacaoRepository.findByMissaoId(missao.getId());
        assertThat(participacoes).hasSize(1);
        assertThat(participacoes.get(0).getPapelMissao()).isEqualTo(PapelMissao.LIDER);
        System.out.println("=== Participação registrada: papel=" + participacoes.get(0).getPapelMissao() + " ===");
    }

    @Test
    void deveGerarRankingDeParticipacao() {
        // Cria participação para ter dados no ranking
        ParticipacaoId pid = new ParticipacaoId(missao.getId(), aventureiro.getId());
        Participacao p = new Participacao();
        p.setId(pid);
        p.setMissao(missao);
        p.setAventureiro(aventureiro);
        p.setPapelMissao(PapelMissao.COMBATENTE);
        p.setRecompensaOuro(new BigDecimal("100.00"));
        participacaoRepository.save(p);
        participacaoRepository.flush();

        List<Object[]> ranking = participacaoRepository.findRankingParticipacao(
                OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1));
        assertThat(ranking).isNotEmpty();
        System.out.println("=== Ranking: " + ranking.size() + " aventureiros ===");
    }

    @Test
    void deveGerarRelatorioMissoes() {
        // Cria participação para ter dados no relatório
        ParticipacaoId pid = new ParticipacaoId(missao.getId(), aventureiro.getId());
        Participacao p = new Participacao();
        p.setId(pid);
        p.setMissao(missao);
        p.setAventureiro(aventureiro);
        p.setPapelMissao(PapelMissao.SUPORTE);
        p.setRecompensaOuro(new BigDecimal("50.00"));
        participacaoRepository.save(p);
        participacaoRepository.flush();

        List<Object[]> relatorio = participacaoRepository.findRelatorioMissoes();
        assertThat(relatorio).isNotEmpty();
        System.out.println("=== Missões no relatório: " + relatorio.size() + " ===");
    }
}
