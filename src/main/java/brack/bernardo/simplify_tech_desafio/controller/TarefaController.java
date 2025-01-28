package brack.bernardo.simplify_tech_desafio.controller;


import brack.bernardo.simplify_tech_desafio.response.GetTarefaResponse;
import brack.bernardo.simplify_tech_desafio.service.TarefaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("v1/tarefas")
@RequiredArgsConstructor
public class TarefaController {

    private final TarefaService service;



    @GetMapping
    public ResponseEntity<List<GetTarefaResponse>> listar() {
        service.listar();
        return ResponseEntity.ok().build();
    }


}
