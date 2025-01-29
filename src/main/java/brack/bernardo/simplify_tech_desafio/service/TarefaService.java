package brack.bernardo.simplify_tech_desafio.service;

import brack.bernardo.simplify_tech_desafio.model.Tarefa;
import brack.bernardo.simplify_tech_desafio.repository.TarefaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TarefaService {

    private final TarefaRepository repository;

    public List<Tarefa> listar(String nome, Boolean realizado, Integer prioridade) {
        return repository.findAll()
                .stream()
                .filter(t -> nome == null || t.getNome().toLowerCase().contains(nome.toLowerCase()))
                .filter(t -> realizado == null || t.getRealizado().equals(realizado))
                .filter(t -> prioridade == null || t.getPrioridade().equals(prioridade))
                .toList();

    }

    public Tarefa buscarPorId(Long id) {
        if(id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tarefa nao encontrada");
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa nao encontrada"));
    }

    public Tarefa salvar(Tarefa tarefa) {
        if(tarefa == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tarefa nao recebida ou mal formada");
        Tarefa.TarefaBuilder tarefaBuilder = Tarefa.builder()
                .nome(tarefa.getNome())
                .descricao(tarefa.getDescricao());

        if(tarefa.getRealizado() != null) {
            tarefaBuilder.realizado(tarefa.getRealizado());
        } else {
            tarefaBuilder.realizado(false);
        }
        if(tarefa.getPrioridade() != null && tarefa.getPrioridade() >= 0 && tarefa.getPrioridade() <= 3) {
            tarefaBuilder.prioridade(tarefa.getPrioridade());
        } else {
            tarefaBuilder.prioridade(0);
        }

        return repository.save(tarefaBuilder.build());
    }

}
