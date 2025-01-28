package brack.bernardo.simplify_tech_desafio.controller;


import brack.bernardo.simplify_tech_desafio.mapper.TarefaMapper;
import brack.bernardo.simplify_tech_desafio.model.Tarefa;
import brack.bernardo.simplify_tech_desafio.request.PostTarefaRequest;
import brack.bernardo.simplify_tech_desafio.response.GetTarefaResponse;
import brack.bernardo.simplify_tech_desafio.service.TarefaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/tarefas")
@RequiredArgsConstructor
public class TarefaController {

    private final TarefaService service;
    private final TarefaMapper mapper;


    @GetMapping
    public ResponseEntity<List<GetTarefaResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean realizado,
            @RequestParam(required = false) Integer prioridade
    ) {
        List<Tarefa> tarefasEncontradas = service.listar(nome, realizado, prioridade);
        List<GetTarefaResponse> body = mapper.toGetTarefaResponseList(tarefasEncontradas);
        return ResponseEntity
                .ok(body);
    }

    @GetMapping("{id}")
    public ResponseEntity<GetTarefaResponse> buscarTarefaPorId(@PathVariable Long id) {
        Tarefa encontrada = service.buscarPorId(id);
        GetTarefaResponse response = mapper.toGetTarefaResponse(encontrada);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<GetTarefaResponse> criarTarefa(@Valid @RequestBody PostTarefaRequest tarefaRequest) {
        Tarefa tarefa = mapper.toTarefa(tarefaRequest);
        Tarefa saved = service.salvar(tarefa);
        return ResponseEntity.ok(mapper.toGetTarefaResponse(saved));
    }




}
