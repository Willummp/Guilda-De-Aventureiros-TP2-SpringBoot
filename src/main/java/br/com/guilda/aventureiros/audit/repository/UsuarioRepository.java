package br.com.guilda.aventureiros.audit.repository;

import br.com.guilda.aventureiros.audit.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT DISTINCT u FROM Usuario u JOIN FETCH u.roles r JOIN FETCH r.permissions")
    List<Usuario> findAllWithRolesAndPermissions();
}
