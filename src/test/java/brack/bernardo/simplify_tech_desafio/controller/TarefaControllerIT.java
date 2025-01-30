package brack.bernardo.simplify_tech_desafio.controller;

import brack.bernardo.simplify_tech_desafio.config.IntegrationTestConfiguration;
import brack.bernardo.simplify_tech_desafio.exception.ApiErrorResponse;
import brack.bernardo.simplify_tech_desafio.response.GetTarefaResponse;
import brack.bernardo.simplify_tech_desafio.utils.CustomPageImpl;
import brack.bernardo.simplify_tech_desafio.utils.FileUtils;
import jakarta.servlet.http.HttpServletRequest;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import net.javacrumbs.jsonunit.core.Option;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import java.util.stream.Stream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = "/sql/init_tarefas_table_schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/sql/clear_tarefa_table.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SqlMergeMode(value = SqlMergeMode.MergeMode.MERGE)
class TarefaControllerIT extends IntegrationTestConfiguration {


    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FileUtils fileUtils;

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

    @Test
    @DisplayName("GET v1/tarefas must return Paginated response and 200 OK")
    void getListOfTarefas_shouldReturnAPageAnd200() {
        String uri = BASE_URL;
        var responseType = new ParameterizedTypeReference<CustomPageImpl<GetTarefaResponse>>(){};
        ResponseEntity<CustomPageImpl<GetTarefaResponse>> response = restTemplate.exchange(uri, HttpMethod.GET, null, responseType);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getBody()).isInstanceOf(Page.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @MethodSource("gerarValoresGetTarefas")
    @DisplayName("GET v1/tarefas must correctly filter tarefas")
    @Sql(value = "/sql/init_tarefa_with_2_tarefas.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getListOfTarefas_shouldReturnCorrectlyFilteredTarefas(String nome, String realizado, String prioridade, String expectedResponseFileName) {
        String uri = BASE_URL + "?nome=" + nome + "&realizado=" + realizado + "&prioridade=" + prioridade;
        String expectedResponse = fileUtils.readResourceFile("/tarefas/%s".formatted(expectedResponseFileName));
        var responseType = new ParameterizedTypeReference<CustomPageImpl<GetTarefaResponse>>(){};
        ResponseEntity<CustomPageImpl<GetTarefaResponse>> response = restTemplate
                .exchange(uri, HttpMethod.GET, null, responseType);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();
        JsonAssertions.assertThatJson(response.getBody().getContent())
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedResponse);
    }

    @ParameterizedTest
    @MethodSource("gerarTarefasMalFormadas")
    void postTarefa_whenTarefaMalformed_shouldReturn400AndApiErrorResponse(String requestBodyFileName, String expectedResponseFileName) {
        String uri = BASE_URL;
        String requestBody = fileUtils.readResourceFile("tarefas/%s".formatted(requestBodyFileName));
        String expectedResponseBody = fileUtils.readResourceFile("tarefas/%s".formatted(expectedResponseFileName));

        HttpEntity<String> request = buildHttpEntity(requestBody);
        ResponseEntity<ApiErrorResponse> response = restTemplate.exchange(uri, HttpMethod.POST, request, ApiErrorResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonAssertions.assertThatJson(response.getBody())
                .whenIgnoringPaths("timestamp", "message")
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedResponseBody);
        JsonAssertions.assertThatJson(response.getBody())
                .inPath("$.timestamp")
                .isNotNull();
        JsonAssertions.assertThatJson(response.getBody())
                .inPath("$.message")
                .isNotNull();
    }

    @ParameterizedTest
    @MethodSource("gerarTarefasBemFormadas")
    void postTarefa_whenTarefaWellFormed_shouldReturn201AndSavedGetTarefaResponse(String requestBodyFileName, String expectedResponseFileName) {
        String uri = BASE_URL;
        String requestBody = fileUtils.readResourceFile("tarefas/%s".formatted(requestBodyFileName));
        String expectedResponseBody = fileUtils.readResourceFile("tarefas/%s".formatted(expectedResponseFileName));
        HttpEntity<String> request = buildHttpEntity(requestBody);
        ResponseEntity<GetTarefaResponse> response = restTemplate.exchange(uri, HttpMethod.POST, request, GetTarefaResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().id()).isNotNull().isInstanceOf(Long.class);
        JsonAssertions.assertThatJson(response.getBody())
                .whenIgnoringPaths("id")
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedResponseBody);
    }

    private static HttpEntity<String> buildHttpEntity(String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(requestBody, headers);

    }

    public static Stream<Arguments> gerarValoresGetTarefas() {
        return Stream.of(
                Arguments.of("","","", "get-tarefas-no-filter-response-200.json"),
                Arguments.of("cafe","","","get-tarefas-nome-cafe-response-200.json"),
                Arguments.of("", "false", "", "get-tarefas-realizado-false-response-200.json"),
                Arguments.of("", "true","2","get-tarefas-realizado-true-prioridade-2-reponse-200.json")
        );
    }

    public static Stream<Arguments> gerarTarefasMalFormadas() {
        return Stream.of(
                Arguments.of("post-tarefa-tudo-null-request.json", "post-tarefa-tudo-null-response-400.json"),
                Arguments.of("post-tarefa-parcialmente-preenchido-request.json", "post-tarefa-parcialmente-preenchido-response-400.json"),
                Arguments.of("post-tarefa-parcialmente-preenchido-2-request.json", "post-tarefa-parcialmente-preenchido-2-response-400.json")
        );
    }

    public static Stream<Arguments> gerarTarefasBemFormadas() {
        return Stream.of(
                Arguments.of("post-tarefa-bem-formada-request.json", "post-tarefa-bem-formada-response-201.json"),
                Arguments.of("post-tarefa-bem-formada-2-request.json", "post-tarefa-bem-formada-2-response-201.json")
        );
    }

}
