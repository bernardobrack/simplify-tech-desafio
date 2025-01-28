package brack.bernardo.simplify_tech_desafio.service;

import brack.bernardo.simplify_tech_desafio.model.Tarefa;
import brack.bernardo.simplify_tech_desafio.repository.TarefaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TarefaService {

    private final TarefaRepository repository;

    public List<Tarefa> listar() {
        return repository.findAll();
    }

}
