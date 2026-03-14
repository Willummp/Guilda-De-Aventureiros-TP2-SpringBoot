package br.com.guilda.aventureiros.controller;

import br.com.guilda.aventureiros.domain.Aventureiro;
import br.com.guilda.aventureiros.domain.Classe;
import br.com.guilda.aventureiros.domain.Companheiro;
import br.com.guilda.aventureiros.dto.AventureiroRequest;
import br.com.guilda.aventureiros.dto.AventureiroResponse;
import br.com.guilda.aventureiros.dto.AventureiroResumoResponse;
import br.com.guilda.aventureiros.dto.CompanheiroRequest;
import br.com.guilda.aventureiros.exception.RecursoNaoEncontradoException;
import br.com.guilda.aventureiros.repository.AventureiroRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoint REST principal para gerenciar o Registro Oficial da Guilda de Aventureiros.
 * Expõe as rotas de listagem, criação, atualização e vinculação de companheiros.
 */
@RestController
@RequestMapping("/aventureiros")
public class AventureiroController {

    private final AventureiroRepository repository;

    public AventureiroController(AventureiroRepository repository) {
        this.repository = repository;
    }

    /**
     * Registra um novo aventureiro na guilda.
     * Por regra de negócio, o aventureiro entra como ativo e sem companheiro.
     *
     * @param request JSON com nome, classe e nivel
     * @return O aventureiro criado (Status 201 Created)
     */
    @PostMapping
    public ResponseEntity<AventureiroResponse> registrar(@Valid @RequestBody AventureiroRequest request) {
        Aventureiro novo = new Aventureiro(request.getNome(), request.getClasse(), request.getNivel());
        Aventureiro salvo = repository.save(novo);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(new AventureiroResponse(salvo));
    }

    /**
     * Lista aventureiros cadastrados, permitindo paginação e filtros.
     *
     * @param classe filtro opcional por classe
     * @param ativo filtro opcional por status (true/false)
     * @param nivel filtro opcional por nível mínimo
     * @param page página desejada (começa em 0)
     * @param size tamanho da página (entre 1 e 50)
     * @return Lista paginada de AventureiroResumoResponse com headers de paginação (X-Total-Count, X-Page, etc)
     */
    @GetMapping
    public ResponseEntity<List<AventureiroResumoResponse>> listar(
            @RequestParam(required = false) Classe classe,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Integer nivel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // Validação básica da paginação
        if (page < 0) page = 0;
        if (size < 1) size = 1;
        if (size > 50) size = 50;

        List<Aventureiro> todosFiltrados = repository.findByFilters(classe, ativo, nivel);
        int totalElements = todosFiltrados.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Recorte da paginação (subList)
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);
        
        List<Aventureiro> pagina;
        if (fromIndex >= totalElements) {
            // Página além dos limites - retorna lista vazia
            pagina = List.of();
        } else {
            pagina = todosFiltrados.subList(fromIndex, toIndex);
        }

        List<AventureiroResumoResponse> responseList = pagina.stream()
                .map(AventureiroResumoResponse::new)
                .collect(Collectors.toList());

        // Configuração dos headers obrigatórios da Guilda
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(totalElements));
        headers.add("X-Page", String.valueOf(page));
        headers.add("X-Size", String.valueOf(size));
        headers.add("X-Total-Pages", String.valueOf(totalPages));

        return ResponseEntity.ok().headers(headers).body(responseList);
    }

    /**
     * Consulta as informações completas de um aventureiro específico (incluindo o companheiro, se existir).
     *
     * @param id identificador do aventureiro
     * @return Dados completos do aventureiro
     */
    @GetMapping("/{id}")
    public ResponseEntity<AventureiroResponse> consultar(@PathVariable Long id) {
        Aventureiro aventureiro = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("aventureiro não encontrado"));

        return ResponseEntity.ok(new AventureiroResponse(aventureiro));
    }

    /**
     * Atualiza dados permitidos de um aventureiro existente (nome, classe, nível).
     * Não permite alterar ID, status ativo ou companheiro.
     *
     * @param id identificador do aventureiro
     * @param request JSON com novos dados
     * @return O aventureiro atualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<AventureiroResponse> atualizar(@PathVariable Long id, @Valid @RequestBody AventureiroRequest request) {
        Aventureiro aventureiro = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("aventureiro não encontrado"));

        aventureiro.setNome(request.getNome());
        aventureiro.setClasse(request.getClasse());
        aventureiro.setNivel(request.getNivel());

        // O save() atualiza porque o ID já existe
        Aventureiro atualizado = repository.save(aventureiro);
        
        return ResponseEntity.ok(new AventureiroResponse(atualizado));
    }

    /**
     * Encerra o vínculo de um aventureiro com a guilda, marcando-o como inativo.
     *
     * @param id identificador do aventureiro
     * @return Status 204 No Content para confirmar sucesso sem corpo na resposta
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> encerrarVinculo(@PathVariable Long id) {
        Aventureiro aventureiro = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("aventureiro não encontrado"));

        aventureiro.setAtivo(false);
        repository.save(aventureiro);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Reativa o vínculo de um aventureiro com a guilda.
     *
     * @param id identificador do aventureiro
     * @return Dados atualizados do aventureiro reativado
     */
    @PatchMapping("/{id}/recrutar")
    public ResponseEntity<AventureiroResponse> recrutar(@PathVariable Long id) {
        Aventureiro aventureiro = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("aventureiro não encontrado"));

        aventureiro.setAtivo(true);
        Aventureiro atualizado = repository.save(aventureiro);

        return ResponseEntity.ok(new AventureiroResponse(atualizado));
    }

    /**
     * Define ou substitui o companheiro de um aventureiro existente.
     *
     * @param id identificador do aventureiro
     * @param request JSON contendo dados do novo companheiro (nome, especie, lealdade)
     * @return Aventureiro atualizado com o companheiro recém-criado
     */
    @PutMapping("/{id}/companheiro")
    public ResponseEntity<AventureiroResponse> definirCompanheiro(@PathVariable Long id, @Valid @RequestBody CompanheiroRequest request) {
        Aventureiro aventureiro = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("aventureiro não encontrado"));

        Companheiro companheiro = new Companheiro(request.getNome(), request.getEspecie(), request.getLealdade());
        aventureiro.setCompanheiro(companheiro);
        Aventureiro atualizado = repository.save(aventureiro);

        return ResponseEntity.ok(new AventureiroResponse(atualizado));
    }

    /**
     * Remove o companheiro vinculado ao aventureiro.
     *
     * @param id identificador do aventureiro
     * @return Confirmação sem corpo da exclusão lógica do companheiro (Status 204 No Content)
     */
    @DeleteMapping("/{id}/companheiro")
    public ResponseEntity<Void> removerCompanheiro(@PathVariable Long id) {
        Aventureiro aventureiro = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("aventureiro não encontrado"));

        aventureiro.setCompanheiro(null);
        repository.save(aventureiro);
        
        return ResponseEntity.noContent().build();
    }
}
