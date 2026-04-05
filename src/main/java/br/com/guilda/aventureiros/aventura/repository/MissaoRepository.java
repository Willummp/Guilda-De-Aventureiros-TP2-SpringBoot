package br.com.guilda.aventureiros.aventura.repository;

import br.com.guilda.aventureiros.aventura.domain.Missao;
import br.com.guilda.aventureiros.aventura.domain.NivelPerigo;
import br.com.guilda.aventureiros.aventura.domain.StatusMissao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface MissaoRepository extends JpaRepository<Missao, Long> {

    /**
     * Filtros por status e nível de perigo sem intervalo de datas.
     * (Consulta separada evita {@code param IS NULL} no SQL, que no PostgreSQL gera
     * "could not determine data type of parameter" com bind nulo.)
     */
    @Query("SELECT m FROM Missao m WHERE " +
           "m.status = COALESCE(:status, m.status) AND " +
           "m.nivelPerigo = COALESCE(:nivelPerigo, m.nivelPerigo)")
    Page<Missao> findByFiltrosSemPeriodo(
            @Param("status") StatusMissao status,
            @Param("nivelPerigo") NivelPerigo nivelPerigo,
            Pageable pageable);

    /**
     * Mesmos filtros de status/perigo + sobreposição de intervalo (ambas as datas obrigatórias no serviço).
     */
    @Query("SELECT m FROM Missao m WHERE " +
           "m.status = COALESCE(:status, m.status) AND " +
           "m.nivelPerigo = COALESCE(:nivelPerigo, m.nivelPerigo) AND " +
           "COALESCE(m.dataInicio, m.createdAt) <= :periodoFim AND " +
           "COALESCE(m.dataTermino, m.dataInicio, m.createdAt) >= :periodoInicio")
    Page<Missao> findByFiltrosComPeriodo(
            @Param("status") StatusMissao status,
            @Param("nivelPerigo") NivelPerigo nivelPerigo,
            @Param("periodoInicio") OffsetDateTime periodoInicio,
            @Param("periodoFim") OffsetDateTime periodoFim,
            Pageable pageable);

}
