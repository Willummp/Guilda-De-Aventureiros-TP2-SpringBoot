package br.com.guilda.aventureiros.audit.repository;

import br.com.guilda.aventureiros.audit.domain.Organizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizacaoRepository extends JpaRepository<Organizacao, Long> {
}
