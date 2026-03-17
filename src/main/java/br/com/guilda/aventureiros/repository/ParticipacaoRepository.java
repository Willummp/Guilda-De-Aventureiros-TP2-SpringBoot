package br.com.guilda.aventureiros.repository;

import br.com.guilda.aventureiros.domain.Participacao;
import br.com.guilda.aventureiros.domain.ParticipacaoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ParticipacaoRepository extends JpaRepository<Participacao, ParticipacaoId> {

    // Participações de uma missão específica (detalhamento - Parte 3)
    @Query("SELECT p FROM Participacao p JOIN FETCH p.aventureiro WHERE p.missao.id = :missaoId")
    List<Participacao> findByMissaoId(@Param("missaoId") Long missaoId);

    // Total de participações de um aventureiro (para visualização completa - Parte 3)
    long countByAventureiroId(@Param("aventureiroId") Long aventureiroId);

    // Última missão de um aventureiro (Parte 3)
    @Query("SELECT p FROM Participacao p WHERE p.aventureiro.id = :aventureiroId ORDER BY p.createdAt DESC")
    List<Participacao> findUltimaMissao(@Param("aventureiroId") Long aventureiroId,
                                        org.springframework.data.domain.Pageable pageable);

    // Ranking de participação - Parte 3
    @Query("SELECT p.aventureiro.id, p.aventureiro.nome, " +
           "COUNT(p), SUM(p.recompensaOuro), SUM(CASE WHEN p.mvp = true THEN 1 ELSE 0 END) " +
           "FROM Participacao p " +
           "WHERE p.createdAt >= COALESCE(:dataInicio, p.createdAt) AND " +
           "      p.createdAt <= COALESCE(:dataFim, p.createdAt) " +
           "GROUP BY p.aventureiro.id, p.aventureiro.nome " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> findRankingParticipacao(
            @Param("dataInicio") OffsetDateTime dataInicio,
            @Param("dataFim") OffsetDateTime dataFim);


    // Relatório de missões com métricas - Parte 3
    @Query("SELECT p.missao.id, p.missao.titulo, p.missao.status, p.missao.nivelPerigo, " +
           "COUNT(p), SUM(p.recompensaOuro) " +
           "FROM Participacao p " +
           "GROUP BY p.missao.id, p.missao.titulo, p.missao.status, p.missao.nivelPerigo " +
           "ORDER BY p.missao.id")
    List<Object[]> findRelatorioMissoes();
}
