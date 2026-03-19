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
     * Busca missões utilizando filtros dinâmicos.
     * O uso de COALESCE permite que, se um parâmetro for nulo, a consulta utilize o próprio valor da coluna,
     * tornando o filtro opcional sem a necessidade de múltiplas queries ou critérios dinâmicos complexos.
     */
    @Query("SELECT m FROM Missao m WHERE " +
           "m.status = COALESCE(:status, m.status) AND " +
           "m.nivelPerigo = COALESCE(:nivelPerigo, m.nivelPerigo) AND " +
           "m.createdAt >= COALESCE(:dataInicio, m.createdAt) AND " +
           "m.createdAt <= COALESCE(:dataFim, m.createdAt)")
    Page<Missao> findByFiltros(
            @Param("status") StatusMissao status,
            @Param("nivelPerigo") NivelPerigo nivelPerigo,
            @Param("dataInicio") OffsetDateTime dataInicio,
            @Param("dataFim") OffsetDateTime dataFim,
            Pageable pageable);

}
