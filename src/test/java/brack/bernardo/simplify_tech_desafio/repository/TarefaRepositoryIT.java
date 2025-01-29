package brack.bernardo.simplify_tech_desafio.repository;


import brack.bernardo.simplify_tech_desafio.config.IntegrationTestConfiguration;
import brack.bernardo.simplify_tech_desafio.model.Tarefa;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import java.util.List;
import java.util.stream.Stream;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(value = "/sql/init_tarefas_table_schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/sql/clear_tarefa_table.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class TarefaRepositoryIT extends IntegrationTestConfiguration {

    @Autowired
    private TarefaRepository tarefaRepository;



    public static Stream<Arguments> gerarFiltrosDeTarefasComValoresNulos() {
        return Stream.of(
                Arguments.of("%cafe%", true, 2, List.of(2L)),
                Arguments.of((Object) null,(Object)null,(Object)null, List.of(1L,2L)),
                Arguments.of("%cafe%", (Object)null,(Object)null, List.of(2L)),
                Arguments.of("%cafe%", true, (Object)null,List.of(2L))

        );
    }

    public static Stream<Arguments> gerarDiferentesPageable() {
        return Stream.of(
                Arguments.of(PageRequest.of(0, 2), List.of(1L,2L)),
                Arguments.of(PageRequest.of(1,1), List.of(2L)),
                Arguments.of(PageRequest.of(0,1), List.of(1L))
        );
    }

    @ParameterizedTest
    @MethodSource("gerarFiltrosDeTarefasComValoresNulos")
    @Sql(value = "/sql/init_tarefa_with_2_tarefas.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findAllByNomeLikeRealizadoAndPrioridadeAceitandoNull_whenThereAreNullParams_mustNotFilterThroughTheNullParam(String nomeFiltro, Boolean realizado, Integer prioridade, List<Long> expectedIdReturned) {
        Page<Tarefa> page =  tarefaRepository.findAllByNomeLikeRealizadoAndPrioridadeAceitandoNull(nomeFiltro, realizado, prioridade, Pageable.unpaged());
        Assertions.assertThat(page.getContent()).hasSize(expectedIdReturned.size());
        Assertions.assertThat(page.getContent().stream().map(Tarefa::getId).toList()).hasSameElementsAs(expectedIdReturned);

    }

    @ParameterizedTest
    @MethodSource("gerarDiferentesPageable")
    @Sql(value = "/sql/init_tarefa_with_2_tarefas.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findAllByNomeLikeRealizadoAndPrioridadeAceitandoNull_mustDoCorrectPaging(Pageable pageable, List<Long> expectedIdReturned) {
        Page<Tarefa> page = tarefaRepository.findAllByNomeLikeRealizadoAndPrioridadeAceitandoNull(null,null,null,pageable);
        Assertions.assertThat(page.getContent()).hasSize(expectedIdReturned.size());
        Assertions.assertThat(page.getSize()).isEqualTo(pageable.getPageSize());
        Assertions.assertThat(page.getNumber()).isEqualTo(pageable.getPageNumber());
        Assertions.assertThat(page.getContent().stream().map(Tarefa::getId).toList()).hasSameElementsAs(expectedIdReturned);
    }
}
