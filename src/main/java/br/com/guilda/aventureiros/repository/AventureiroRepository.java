package br.com.guilda.aventureiros.repository;

import br.com.guilda.aventureiros.domain.Aventureiro;
import br.com.guilda.aventureiros.domain.Classe;
import br.com.guilda.aventureiros.domain.Companheiro;
import br.com.guilda.aventureiros.domain.Especie;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Repositório em memória para simular um banco de dados de Aventureiros.
 * <p>
 * O armazenamento é feito através de uma ArrayList, inicializada com 100 registros
 * como exigido nas restrições originais do projeto (TP1).
 * </p>
 */
@Repository
public class AventureiroRepository {

    private final List<Aventureiro> aventureiros = new ArrayList<>();
    
    // Simula uma sequence de banco de dados para IDs garantindo unicidade básica no mock
    private final AtomicLong idGenerator = new AtomicLong(1);

    public AventureiroRepository() {
        iniciarRegistrosBase();
    }

    /**
     * Salva um novo aventureiro no banco de dados em memória.
     * Atribui um ID sequencial antes de adicionar na lista.
     *
     * @param aventureiro objeto aventureiro para ser salvo
     * @return o aventureiro salvo com ID preenchido
     */
    public Aventureiro save(Aventureiro aventureiro) {
        if (aventureiro.getId() == null) {
            aventureiro.setId(idGenerator.getAndIncrement());
        } else {
            // Em caso de update, precisamos substituir o antigo pelo novo.
            // Para manter a simplicidade da ArrayList, removemos o objeto antigo com mesmo ID e readicionamos.
            aventureiros.removeIf(a -> a.getId().equals(aventureiro.getId()));
        }
        aventureiros.add(aventureiro);
        return aventureiro;
    }

    /**
     * Busca um aventureiro específico pelo seu identificador (ID).
     *
     * @param id identificador do aventureiro
     * @return Optional contendo o aventureiro, ou vazio se não encontrado
     */
    public Optional<Aventureiro> findById(Long id) {
        return aventureiros.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

    /**
     * Filtra e retorna a lista de aventureiros que batem com os critérios exigidos.
     *
     * @param classe opcional: filtrar por classe do aventureiro
     * @param ativo opcional: filtrar por membros ativos ou inativos
     * @param nivelMinimo opcional: filtrar por nível maior ou igual
     * @return Lista paginada conforme requisitos, antes do recorte de páginas
     */
    public List<Aventureiro> findByFilters(Classe classe, Boolean ativo, Integer nivelMinimo) {
        return aventureiros.stream()
                .filter(a -> classe == null || a.getClasse() == classe)
                .filter(a -> ativo == null || a.isAtivo() == ativo)
                .filter(a -> nivelMinimo == null || a.getNivel() >= nivelMinimo)
                // É exigido ordenação por ID crescente
                .sorted((a1, a2) -> a1.getId().compareTo(a2.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Inicializa o banco de dados em memória com 100 aventureiros.
     * Cria 50 aventureiros sem companheiros, e 50 com companheiros variando entre as classes e espécies.
     */
    private void iniciarRegistrosBase() {
        Classe[] classes = Classe.values();
        Especie[] especies = Especie.values();

        for (int i = 1; i <= 100; i++) {
            Aventureiro a = new Aventureiro();
            a.setId(idGenerator.getAndIncrement());
            a.setNome("Aventureiro " + i);
            a.setClasse(classes[i % classes.length]);
            a.setNivel((i % 20) + 1); // Níveis entre 1 e 20
            a.setAtivo(i % 10 != 0); // 1 a cada 10 estarão inativos

            if (i > 50) { // Metade final tem companheiros
                Companheiro comp = new Companheiro();
                comp.setNome("Companheiro de " + a.getNome());
                comp.setEspecie(especies[i % especies.length]);
                comp.setLealdade((i % 100) + 1); // Lealdade entre 1 e 100
                a.setCompanheiro(comp);
            }
            aventureiros.add(a);
        }
    }
}
