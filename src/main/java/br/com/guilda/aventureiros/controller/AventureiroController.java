package br.com.guilda.aventureiros.controller;

import br.com.guilda.aventureiros.audit.domain.Organizacao;
import br.com.guilda.aventureiros.audit.domain.Usuario;
import br.com.guilda.aventureiros.audit.repository.OrganizacaoRepository;
import br.com.guilda.aventureiros.audit.repository.UsuarioRepository;
import br.com.guilda.aventureiros.domain.Aventureiro;
import br.com.guilda.aventureiros.domain.Classe;
import br.com.guilda.aventureiros.domain.Companheiro;
import br.com.guilda.aventureiros.dto.AventureiroRequest;
import br.com.guilda.aventureiros.dto.AventureiroResponse;
import br.com.guilda.aventureiros.dto.AventureiroResumoResponse;
import br.com.guilda.aventureiros.dto.CompanheiroRequest;
import br.com.guilda.aventureiros.exception.RecursoNaoEncontradoException;
import br.com.guilda.aventureiros.repository.AventureiroRepository;
import br.com.guilda.aventureiros.repository.ParticipacaoRepository;
import br.com.guilda.aventureiros.domain.Participacao;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoint REST para gerenciar o Registro Oficial da Guilda de Aventureiros.
 */
@RestController
@RequestMapping("/aventureiros")
public class AventureiroController {

    private final AventureiroRepository aventureiroRepository;
    private final OrganizacaoRepository organizacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ParticipacaoRepository participacaoRepository;

    public AventureiroController(AventureiroRepository aventureiroRepository,
                                  OrganizacaoRepository organizacaoRepository,
                                  UsuarioRepository usuarioRepository,
                                  ParticipacaoRepository participacaoRepository) {
        this.aventureiroRepository = aventureiroRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.participacaoRepository = participacaoRepository;
    }

    @PostMapping
    public ResponseEntity<AventureiroResponse> registrar(@Valid @RequestBody AventureiroRequest request) {
        Organizacao org = organizacaoRepository.findById(request.getOrganizacaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("organização não encontrada"));
        Usuario usuario = usuarioRepository.findById(request.getUsuarioResponsavelId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("usuário responsável não encontrado"));

        Aventureiro novo = new Aventureiro(request.getNome(), request.getClasse(), request.getNivel());
        novo.setOrganizacao(org);
        novo.setUsuarioResponsavel(usuario);
        Aventureiro salvo = aventureiroRepository.save(novo);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AventureiroResponse(salvo));
    }

    @GetMapping
    public ResponseEntity<List<AventureiroResumoResponse>> listar(
            @RequestParam(required = false) Classe classe,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Integer nivel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome") String sortBy
    ) {
        if (page < 0) page = 0;
        if (size < 1) size = 1;
        if (size > 50) size = 50;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Aventureiro> resultado = aventureiroRepository.findByFiltros(classe, ativo, nivel, pageable);

        List<AventureiroResumoResponse> body = resultado.getContent().stream()
                .map(AventureiroResumoResponse::new)
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(resultado.getTotalElements()));
        headers.add("X-Page", String.valueOf(page));
        headers.add("X-Size", String.valueOf(size));
        headers.add("X-Total-Pages", String.valueOf(resultado.getTotalPages()));

        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping("/busca")
    public ResponseEntity<List<AventureiroResumoResponse>> buscarPorNome(
            @RequestParam String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome"));
        Page<Aventureiro> resultado = aventureiroRepository.findByNomeContainingIgnoreCase(nome, pageable);

        List<AventureiroResumoResponse> body = resultado.getContent().stream()
                .map(AventureiroResumoResponse::new)
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(resultado.getTotalElements()));
        headers.add("X-Page", String.valueOf(page));
        headers.add("X-Total-Pages", String.valueOf(resultado.getTotalPages()));

        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AventureiroResponse> consultar(@PathVariable Long id) {
        // Requisito: Perfil Completo (Aventureiro + Companheiro + Total Participações + Última Missão)
        Aventureiro aventureiro = aventureiroRepository.findByIdWithCompanheiro(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("aventureiro não encontrado"));
        
        AventureiroResponse response = new AventureiroResponse(aventureiro);
        
        long totalMissoes = participacaoRepository.countByAventureiroId(id);
        response.setTotalMissoes(totalMissoes);
        
        List<Participacao> historico = participacaoRepository.findUltimaMissao(id, PageRequest.of(0, 1));
        if (!historico.isEmpty()) {
            response.setUltimaMissao(historico.get(0).getMissao().getTitulo());
        } else {
            response.setUltimaMissao("Nenhuma missão concluída.");
        }
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AventureiroResponse> atualizar(@PathVariable Long id, @Valid @RequestBody AventureiroRequest request) {
        Aventureiro aventureiro = aventureiroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("aventureiro não encontrado"));

        aventureiro.setNome(request.getNome());
        aventureiro.setClasse(request.getClasse());
        aventureiro.setNivel(request.getNivel());

        return ResponseEntity.ok(new AventureiroResponse(aventureiroRepository.save(aventureiro)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> encerrarVinculo(@PathVariable Long id) {
        Aventureiro aventureiro = aventureiroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("aventureiro não encontrado"));
        aventureiro.setAtivo(false);
        aventureiroRepository.save(aventureiro);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/recrutar")
    public ResponseEntity<AventureiroResponse> recrutar(@PathVariable Long id) {
        Aventureiro aventureiro = aventureiroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("aventureiro não encontrado"));
        aventureiro.setAtivo(true);
        return ResponseEntity.ok(new AventureiroResponse(aventureiroRepository.save(aventureiro)));
    }

    @PutMapping("/{id}/companheiro")
    public ResponseEntity<AventureiroResponse> definirCompanheiro(
            @PathVariable Long id, @Valid @RequestBody CompanheiroRequest request) {
        Aventureiro aventureiro = aventureiroRepository.findByIdWithCompanheiro(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("aventureiro não encontrado"));

        Companheiro companheiro = aventureiro.getCompanheiro();
        if (companheiro == null) {
            companheiro = new Companheiro();
            companheiro.setAventureiro(aventureiro);
        }
        companheiro.setNome(request.getNome());
        companheiro.setEspecie(request.getEspecie());
        companheiro.setLealdade(request.getLealdade());
        aventureiro.setCompanheiro(companheiro);

        return ResponseEntity.ok(new AventureiroResponse(aventureiroRepository.save(aventureiro)));
    }

    @DeleteMapping("/{id}/companheiro")
    public ResponseEntity<Void> removerCompanheiro(@PathVariable Long id) {
        Aventureiro aventureiro = aventureiroRepository.findByIdWithCompanheiro(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("aventureiro não encontrado"));
        aventureiro.setCompanheiro(null);
        aventureiroRepository.save(aventureiro);
        return ResponseEntity.noContent().build();
    }
}
