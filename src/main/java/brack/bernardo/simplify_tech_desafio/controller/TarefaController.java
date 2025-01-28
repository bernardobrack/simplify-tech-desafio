package brack.bernardo.simplify_tech_desafio.controller;


import brack.bernardo.simplify_tech_desafio.mapper.TarefaMapper;
import brack.bernardo.simplify_tech_desafio.model.Tarefa;
import brack.bernardo.simplify_tech_desafio.request.PostTarefaRequest;
import brack.bernardo.simplify_tech_desafio.response.GetTarefaResponse;
import brack.bernardo.simplify_tech_desafio.service.TarefaService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<GetTarefaResponse>> listar() {
        List<Tarefa> tarefasEncontradas = service.listar();
        List<GetTarefaResponse> body = mapper.toGetTarefaResponseList(tarefasEncontradas);
        return ResponseEntity
                .ok(body);
    }

    @PostMapping
    public ResponseEntity<GetTarefaResponse> criarTarefa(@RequestBody PostTarefaRequest tarefaRequest) {
        Tarefa tarefa = mapper.toTarefa(tarefaRequest);
        Tarefa saved = service.salvar(tarefa);
        return ResponseEntity.ok(mapper.toGetTarefaResponse(saved));
    }


}
