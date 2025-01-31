package brack.bernardo.simplify_tech_desafio.controller;


import brack.bernardo.simplify_tech_desafio.mapper.TarefaMapper;
import brack.bernardo.simplify_tech_desafio.model.Tarefa;
import brack.bernardo.simplify_tech_desafio.request.PatchTarefaRequest;
import brack.bernardo.simplify_tech_desafio.request.PostTarefaRequest;
import brack.bernardo.simplify_tech_desafio.response.GetTarefaResponse;
import brack.bernardo.simplify_tech_desafio.service.TarefaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("v1/tarefas")
@RequiredArgsConstructor
public class TarefaController {

    private final TarefaService service;
    private final TarefaMapper mapper;


    @GetMapping
    public ResponseEntity<Page<GetTarefaResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean realizado,
            @RequestParam(required = false) Integer prioridade,
            Pageable pageable
    ) {
        Page<Tarefa> tarefasEncontradas = service.listar(nome, realizado, prioridade, pageable);
        var responsePage = tarefasEncontradas.map(mapper::toGetTarefaResponse);

        return ResponseEntity
                .ok(responsePage);
    }

    @PostMapping
    public ResponseEntity<GetTarefaResponse> criarTarefa(@Valid @RequestBody PostTarefaRequest tarefaRequest) {
        Tarefa tarefa = mapper.toTarefa(tarefaRequest);
        Tarefa saved = service.salvar(tarefa);
        return new ResponseEntity<>(mapper.toGetTarefaResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<GetTarefaResponse> buscarTarefaPorId(@PathVariable Long id) {
        Tarefa encontrada = service.buscarPorId(id);
        GetTarefaResponse response = mapper.toGetTarefaResponse(encontrada);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> removerTarefa(@PathVariable Long id) {
        service.deletarTarefa(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("{id}")
    public ResponseEntity<Void> atualizarTarefa(@PathVariable Long id, @RequestBody PatchTarefaRequest tarefaRequest) {
        Tarefa tarefa = mapper.toTarefa(tarefaRequest);
        service.atualizarTarefa(id, tarefa);
        return ResponseEntity.noContent().build();
    }








}
