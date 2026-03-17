package br.com.guilda.aventureiros.audit;

import br.com.guilda.aventureiros.audit.domain.Organizacao;
import br.com.guilda.aventureiros.audit.domain.Role;
import br.com.guilda.aventureiros.audit.domain.Usuario;
import br.com.guilda.aventureiros.audit.repository.OrganizacaoRepository;
import br.com.guilda.aventureiros.audit.repository.RoleRepository;
import br.com.guilda.aventureiros.audit.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para o schema audit (Parte 1).
 * Verifica mapeamento JPA com banco PostgreSQL real (Docker).
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=update"
})
@Transactional
class AuditJpaTest {

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Verifica que é possível listar organizações existentes no banco legado.
     */
    @Test
    void deveListarOrganizacoes() {
        List<Organizacao> orgs = organizacaoRepository.findAll();
        assertThat(orgs).isNotNull();
        System.out.println("=== Organizações encontradas: " + orgs.size() + " ===");
    }

    /**
     * Verifica que usuários com roles são carregados corretamente.
     */
    @Test
    void deveListarUsuariosComRoles() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        assertThat(usuarios).isNotNull();
        System.out.println("=== Usuários encontrados: " + usuarios.size() + " ===");
        for (Usuario u : usuarios) {
            System.out.println("  Usuário: " + u.getNome() + " | Org: " + u.getOrganizacao().getNome()
                    + " | Roles: " + u.getRoles().size());
        }
    }

    /**
     * Verifica que roles com suas permissions são carregadas corretamente.
     */
    @Test
    void deveListarRolesComPermissions() {
        List<Role> roles = roleRepository.findAllWithPermissions();
        assertThat(roles).isNotNull();
        System.out.println("=== Roles encontradas: " + roles.size() + " ===");
        for (Role r : roles) {
            System.out.println("  Role: " + r.getNome() + " | Permissions: " + r.getPermissions().size());
        }
    }

    /**
     * Verifica que é possível persistir um novo usuário associado a uma organização existente.
     */
    @Test
    void devePersistirNovoUsuarioEmOrganizacaoExistente() {
        List<Organizacao> orgs = organizacaoRepository.findAll();
        assertThat(orgs).isNotEmpty();

        Organizacao org = orgs.get(0);

        Usuario novoUsuario = new Usuario();
        novoUsuario.setOrganizacao(org);
        novoUsuario.setNome("Usuário Teste TP2");
        novoUsuario.setEmail("teste.tp2@" + org.getNome().toLowerCase().replace(" ", "") + ".com");
        novoUsuario.setSenhaHash("hash_placeholder");
        novoUsuario.setStatus("ATIVO");
        novoUsuario.setCreatedAt(java.time.OffsetDateTime.now());
        novoUsuario.setUpdatedAt(java.time.OffsetDateTime.now());

        Usuario salvo = usuarioRepository.save(novoUsuario);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getOrganizacao().getId()).isEqualTo(org.getId());
        System.out.println("=== Usuário persistido: ID=" + salvo.getId() + " | Org=" + org.getNome() + " ===");
    }
}
