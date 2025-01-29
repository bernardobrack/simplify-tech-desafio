package brack.bernardo.simplify_tech_desafio.repository;

import brack.bernardo.simplify_tech_desafio.model.Tarefa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface TarefaRepository extends JpaRepository<Tarefa, Long> {


    @Query("SELECT t FROM Tarefa t WHERE (:nomeFiltro IS NULL OR t.nome LIKE :nomeFiltro) AND (:realizado IS NULL OR :realizado = t.realizado) AND (:prioridade IS NULL OR :prioridade = t.prioridade)")
    Page<Tarefa> findAllByNomeLikeRealizadoAndPrioridadeAceitandoNull(String nomeFiltro, Boolean realizado, Integer prioridade, Pageable pageable);
}
