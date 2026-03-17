package br.com.guilda.aventureiros.repository;

import br.com.guilda.aventureiros.domain.Aventureiro;
import br.com.guilda.aventureiros.domain.Classe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AventureiroRepository extends JpaRepository<Aventureiro, Long> {

    // Listagem com filtros (Parte 3)
    @Query("SELECT a FROM Aventureiro a WHERE " +
           "(:classe IS NULL OR a.classe = :classe) AND " +
           "(:ativo IS NULL OR a.ativo = :ativo) AND " +
           "(:nivelMinimo IS NULL OR a.nivel >= :nivelMinimo)")
    Page<Aventureiro> findByFiltros(
            @Param("classe") Classe classe,
            @Param("ativo") Boolean ativo,
            @Param("nivelMinimo") Integer nivelMinimo,
            Pageable pageable);

    // Busca por nome parcial (Parte 3)
    Page<Aventureiro> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    // Visualização completa com companheiro (Parte 3)
    @Query("SELECT a FROM Aventureiro a LEFT JOIN FETCH a.companheiro WHERE a.id = :id")
    Optional<Aventureiro> findByIdWithCompanheiro(@Param("id") Long id);
}
