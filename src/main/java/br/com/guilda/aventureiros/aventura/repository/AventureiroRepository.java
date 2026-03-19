package br.com.guilda.aventureiros.aventura.repository;

import br.com.guilda.aventureiros.aventura.domain.Aventureiro;
import br.com.guilda.aventureiros.aventura.domain.Classe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AventureiroRepository extends JpaRepository<Aventureiro, Long> {

    /**
     * Busca aventureiros com filtros opcionais de classe, status ativo e nível mínimo.
     * Utilizamos a lógica ':param IS NULL OR a.campo = :param' para simular filtros dinâmicos em uma única query JPQL.
     */
    @Query("SELECT a FROM Aventureiro a WHERE " +
           "(:classe IS NULL OR a.classe = :classe) AND " +
           "(:ativo IS NULL OR a.ativo = :ativo) AND " +
           "(:nivelMinimo IS NULL OR a.nivel >= :nivelMinimo)")
    Page<Aventureiro> findByFiltros(
            @Param("classe") Classe classe,
            @Param("ativo") Boolean ativo,
            @Param("nivelMinimo") Integer nivelMinimo,
            Pageable pageable);

    /**
     * Busca aventureiros por parte do nome, ignorando maiúsculas e minúsculas.
     */
    Page<Aventureiro> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    /**
     * Busca um aventureiro pelo ID e já carrega seu companheiro em uma única consulta (JOIN FETCH).
     * Isso evita o problema do SELECT N+1 ao acessar o companheiro animal.
     */
    @Query("SELECT a FROM Aventureiro a LEFT JOIN FETCH a.companheiro WHERE a.id = :id")
    Optional<Aventureiro> findByIdWithCompanheiro(@Param("id") Long id);
}
