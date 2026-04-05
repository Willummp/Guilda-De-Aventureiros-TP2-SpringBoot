package br.com.guilda.aventureiros.aventura.controller;

import br.com.guilda.aventureiros.aventura.domain.Missao;
import br.com.guilda.aventureiros.aventura.domain.NivelPerigo;
import br.com.guilda.aventureiros.aventura.domain.StatusMissao;
import br.com.guilda.aventureiros.aventura.dto.MissaoDetalheResponse;
import br.com.guilda.aventureiros.aventura.dto.MissaoResumoResponse;
import br.com.guilda.aventureiros.aventura.dto.ParticipacaoNaMissaoResponse;
import br.com.guilda.aventureiros.aventura.dto.ParticipacaoRequest;
import br.com.guilda.aventureiros.aventura.exception.RecursoNaoEncontradoException;
import br.com.guilda.aventureiros.aventura.repository.MissaoRepository;
import br.com.guilda.aventureiros.aventura.repository.ParticipacaoRepository;
import br.com.guilda.aventureiros.aventura.service.ParticipacaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/missoes")
public class MissaoController {

    private final MissaoRepository missaoRepository;
    private final ParticipacaoRepository participacaoRepository;
    private final ParticipacaoService participacaoService;

    public MissaoController(MissaoRepository missaoRepository,
                            ParticipacaoRepository participacaoRepository,
                            ParticipacaoService participacaoService) {
        this.missaoRepository = missaoRepository;
        this.participacaoRepository = participacaoRepository;
        this.participacaoService = participacaoService;
    }

    @GetMapping
    public ResponseEntity<List<MissaoResumoResponse>> listar(
            @RequestParam(required = false) StatusMissao status,
            @RequestParam(required = false) NivelPerigo nivelPerigo,
            @RequestParam(required = false) OffsetDateTime periodoInicio,
            @RequestParam(required = false) OffsetDateTime periodoFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "titulo") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        if (page < 0) page = 0;
        if (size < 1) size = 1;
        if (size > 50) size = 50;

        Sort sort = sortMissao(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Missao> resultado = (periodoInicio != null && periodoFim != null)
                ? missaoRepository.findByFiltrosComPeriodo(status, nivelPerigo, periodoInicio, periodoFim, pageable)
                : missaoRepository.findByFiltrosSemPeriodo(status, nivelPerigo, pageable);

        List<MissaoResumoResponse> body = resultado.getContent().stream()
                .map(MissaoResumoResponse::new)
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(resultado.getTotalElements()));
        headers.add("X-Page", String.valueOf(page));
        headers.add("X-Size", String.valueOf(size));
        headers.add("X-Total-Pages", String.valueOf(resultado.getTotalPages()));

        return ResponseEntity.ok().headers(headers).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MissaoDetalheResponse> detalhar(@PathVariable Long id) {
        Missao missao = missaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("missão não encontrada"));
        List<ParticipacaoNaMissaoResponse> participantes = participacaoRepository.findByMissaoId(id).stream()
                .map(ParticipacaoNaMissaoResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new MissaoDetalheResponse(missao, participantes));
    }

    @PostMapping("/{missaoId}/participacoes")
    public ResponseEntity<ParticipacaoNaMissaoResponse> registrarParticipacao(
            @PathVariable Long missaoId,
            @Valid @RequestBody ParticipacaoRequest request
    ) {
        var salva = participacaoService.registrar(missaoId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ParticipacaoNaMissaoResponse(salva));
    }

    private static Sort sortMissao(String sortBy, String sortDir) {
        String key = sortBy == null ? "" : sortBy.toLowerCase();
        String prop;
        if ("status".equals(key)) {
            prop = "status";
        } else if ("createdat".equals(key) || "created_at".equals(key)) {
            prop = "createdAt";
        } else if ("nivelperigo".equals(key) || "nivel_perigo".equals(key)) {
            prop = "nivelPerigo";
        } else {
            prop = "titulo";
        }
        Sort.Direction dir = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, prop);
    }
}
