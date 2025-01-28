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

    public Tarefa salvar(Tarefa tarefa) {
         Tarefa.TarefaBuilder tarefaBuilder = Tarefa.builder()
                .nome(tarefa.getNome())
                .descricao(tarefa.getDescricao());

        if(tarefa.getRealizado() != null) {
            tarefaBuilder.realizado(tarefa.getRealizado());
        } else {
            tarefaBuilder.realizado(false);
        }
        if(tarefa.getPrioridade() != null && tarefa.getPrioridade() > 0 && tarefa.getPrioridade() < 3) {
            tarefaBuilder.prioridade(tarefa.getPrioridade());
        } else {
            tarefaBuilder.prioridade(0);
        }

        return repository.save(tarefaBuilder.build());
    }

}
