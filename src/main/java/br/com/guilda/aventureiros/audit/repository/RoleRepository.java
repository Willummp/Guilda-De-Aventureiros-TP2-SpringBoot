package br.com.guilda.aventureiros.audit.repository;

import br.com.guilda.aventureiros.audit.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("SELECT DISTINCT r FROM Role r JOIN FETCH r.permissions")
    List<Role> findAllWithPermissions();
}
