package brack.bernardo.simplify_tech_desafio.controller;

import brack.bernardo.simplify_tech_desafio.config.IntegrationTestConfiguration;
import brack.bernardo.simplify_tech_desafio.response.GetTarefaResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = "/sql/init_tarefas_table_schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/sql/clear_tarefa_table.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SqlMergeMode(value = SqlMergeMode.MergeMode.MERGE)
class TarefaControllerIT extends IntegrationTestConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String BASE_URL = "/v1/tarefas";


    @Test
    @DisplayName("GET v1/tarefas/{id} when TAREFA with corresponding ID doesnt exist must return 404 NOT_FOUND")
    void getSingleTarefaById_whenIdDoesntExist_shouldReturn404() {
        String uri = BASE_URL + "/1";
        ResponseEntity<GetTarefaResponse> response = restTemplate.exchange(uri, HttpMethod.GET, null, GetTarefaResponse.class);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    @DisplayName("GET v1/tarefas/{id} when TAREFA with corresponding ID exists, must return it and 200 OK")
    @Sql(value = "/sql/init_tarefa_with_2_tarefas.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getSingleTarefaById_whenIdExists_shouldReturn200AndCorrectTarefa() {
        String uri = BASE_URL + "/2";
        ResponseEntity<GetTarefaResponse> responseEntity = restTemplate.getForEntity(uri, GetTarefaResponse.class);
        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isNotNull()
                .hasFieldOrPropertyWithValue("id", 2L)
                .hasFieldOrPropertyWithValue("nome", "Tomar cafe")
                .hasFieldOrPropertyWithValue("descricao", "Devo tomar algumas xicaras de cafe durante o dia.")
                .hasFieldOrPropertyWithValue("realizado", true)
                .hasFieldOrPropertyWithValue("prioridade", 2);

    }


}
