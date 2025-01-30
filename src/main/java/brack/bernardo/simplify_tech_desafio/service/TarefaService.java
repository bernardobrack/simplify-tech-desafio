package brack.bernardo.simplify_tech_desafio.service;

import brack.bernardo.simplify_tech_desafio.exception.ContentNotFoundException;
import brack.bernardo.simplify_tech_desafio.exception.MalformedContentException;
import brack.bernardo.simplify_tech_desafio.model.Tarefa;
import brack.bernardo.simplify_tech_desafio.repository.TarefaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TarefaService {

    private final TarefaRepository repository;

    public Page<Tarefa> listar(String nome, Boolean realizado, Integer prioridade, Pageable pageable) {

        String filtroNome = nome == null ? "%" : "%" + nome + "%";
        Page<Tarefa> pagina = repository.findAllByNomeLikeRealizadoAndPrioridadeAceitandoNull(filtroNome, realizado, prioridade, pageable);

        return pagina;

    }

    public Tarefa buscarPorId(Long id) {
        if(id == null) throw new MalformedContentException("Tarefa nao encontrada");
        return repository.findById(id).orElseThrow(() -> new ContentNotFoundException("Tarefa nao encontrada"));
    }

    public Tarefa salvar(Tarefa tarefa) {
        if(tarefa == null) throw new MalformedContentException("Tarefa nao recebida ou mal formada");
        forcarValorPadraoQueandoInvalido(tarefa);


        return repository.save(tarefa);
    }

    private void forcarValorPadraoQueandoInvalido(Tarefa tarefa) {
        if(tarefa.getPrioridade() == null || tarefa.getPrioridade() < 0 || tarefa.getPrioridade() > 3) {
            tarefa.setPrioridade(0);
        }
        if(tarefa.getRealizado() == null) tarefa.setRealizado(false);
    }

}
