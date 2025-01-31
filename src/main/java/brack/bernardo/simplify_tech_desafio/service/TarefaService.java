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
        return repository.findAllByNomeLikeRealizadoAndPrioridadeAceitandoNull(filtroNome, realizado, prioridade, pageable);

    }

    public Tarefa buscarPorId(Long id) {
        if(id == null) throw new MalformedContentException("Tarefa nao encontrada");
        return repository.findById(id).orElseThrow(() -> new ContentNotFoundException("Tarefa nao encontrada"));
    }

    public Tarefa salvar(Tarefa tarefa) {
        if(tarefa == null) throw new MalformedContentException("Tarefa nao recebida ou mal formada");
        forcarValorPadraoQuandoInvalido(tarefa);


        return repository.save(tarefa);
    }

    private void forcarValorPadraoQuandoInvalido(Tarefa tarefa) {
        if(!isPrioridadeValida(tarefa.getPrioridade())) {
            tarefa.setPrioridade(0);
        }
        if(tarefa.getRealizado() == null) tarefa.setRealizado(false);
    }
    private boolean isPrioridadeValida(Integer prioridade) {
        return !(prioridade == null || prioridade < 0 || prioridade > 3);
    }

    public void deletarTarefa(Long id) {

        repository.deleteById(id);
    }

    public void atualizarTarefa(Long id, Tarefa tarefa) {
        Tarefa found = repository.findById(id).orElseThrow(() -> new ContentNotFoundException("Tarefa nao encontrada"));
        boolean changeNome = false, changePrioridade = false, changeDescricao = false;
        if(tarefa.getPrioridade() != null) {
            assertPrioridadeValid(tarefa.getPrioridade());
            changePrioridade = true;
        }
        if(tarefa.getNome() != null) {
            assertNomeValid(tarefa.getNome());
            changeNome = true;
        }
        if(tarefa.getDescricao() != null) {
            assertDescricaoValid(tarefa.getDescricao());
            changeDescricao = true;
        }

        if(changePrioridade) found.setPrioridade(tarefa.getPrioridade());
        if(tarefa.getRealizado() != null) found.setRealizado(tarefa.getRealizado());
        if(changeNome) found.setNome(tarefa.getNome());
        if(changeDescricao) found.setDescricao(tarefa.getDescricao());
        repository.save(found);
    }

    private void assertNomeValid(String nome) {
        if(nome.trim().isBlank()) throw new MalformedContentException("Nome invalido");
    }

    private void assertDescricaoValid(String descricao) {
        if(descricao.trim().isBlank()) throw new MalformedContentException("Descricao invalida");
    }

    private void assertPrioridadeValid(Integer prioridade) {
        if(!isPrioridadeValida(prioridade)) throw new MalformedContentException("Prioridade invalida");
    }
}
