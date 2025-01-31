package brack.bernardo.simplify_tech_desafio.controller;

import brack.bernardo.simplify_tech_desafio.config.IntegrationTestConfiguration;
import brack.bernardo.simplify_tech_desafio.exception.ApiErrorResponse;
import brack.bernardo.simplify_tech_desafio.model.Tarefa;
import brack.bernardo.simplify_tech_desafio.repository.TarefaRepository;
import brack.bernardo.simplify_tech_desafio.response.GetTarefaResponse;
import brack.bernardo.simplify_tech_desafio.service.TarefaService;
import brack.bernardo.simplify_tech_desafio.utils.CustomPageImpl;
import brack.bernardo.simplify_tech_desafio.utils.FileUtils;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import net.javacrumbs.jsonunit.core.Option;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
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

    @MockitoSpyBean
    private TarefaService service;

    @MockitoSpyBean
    private TarefaRepository repository;

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
    @DisplayName("POST v1/tarefas when tarefa malformed should return 400 BAD_REQUEST and ApiErrorResponse")
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
    @DisplayName("POST v1/tarefas when tarefa well formed should return 201 CREATED and saved GetTarefaResponse")
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

    @ParameterizedTest
    @ValueSource(strings = {"1", "10"})
    @Sql(value = "/sql/init_tarefa_with_2_tarefas.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("DELETE v1/tarefas/{id} must always return 204 NO_CONTENT")
    void deleteTarefa_shouldAlwaysReturn204NoContent(String id) {
        String uri = BASE_URL + "/%s".formatted(id);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(uri, HttpMethod.DELETE, null, Void.class);

        Assertions.assertThat(responseEntity).isNotNull();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Assertions.assertThat(responseEntity.getBody()).isNull();

    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "10"})
    @Sql(value = "/sql/init_tarefa_with_2_tarefas.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("DELETE v1/tarefas/{id} must always call service.deletarTarefa()")
    void deleteTarefa_shouldAlwaysCallServiceDeletarTarefa(String id) {
        String uri = BASE_URL + "/%s".formatted(id);
        restTemplate.exchange(uri, HttpMethod.DELETE, null, Void.class);
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

        BDDMockito.verify(service, BDDMockito.times(1)).deletarTarefa(captor.capture());

        Assertions.assertThat(captor.getValue()).isEqualTo(Long.parseLong(id));

    }

    @Test
    void patchTarefa_shouldAlwaysCallServiceAtualizarTarefaWithCorrectArguments() {
        String uri = BASE_URL + "/1";
        String emptyBody = fileUtils.readResourceFile("/tarefas/post-tarefa-tudo-null-request.json");
        HttpEntity<String> request = buildHttpEntity(emptyBody);

        restTemplate.exchange(uri, HttpMethod.PATCH, request, Void.class);

        BDDMockito.verify(service, BDDMockito.times(1)).atualizarTarefa(BDDMockito.eq(1L), BDDMockito.any(Tarefa.class));

    }

    @Test
    void patchTarefa_shouldReturn400BadRequestWhenBodyRequestIsNull() {
        String uri = BASE_URL + "/1";
        HttpEntity<String> request = buildHttpEntity(null);
        ResponseEntity<ApiErrorResponse> response = restTemplate.exchange(uri, HttpMethod.PATCH, request, ApiErrorResponse.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void patchTarefa_shouldReturn404NotFoundWhenTarefaNotFound() {
        String uri = BASE_URL + "/1";
        String emptyBody = fileUtils.readResourceFile("/tarefas/post-tarefa-tudo-null-request.json");
        HttpEntity<String> request = buildHttpEntity(emptyBody);

        ResponseEntity<ApiErrorResponse> response = restTemplate.exchange(uri, HttpMethod.PATCH, request, ApiErrorResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @ParameterizedTest
    @MethodSource("gerarPatchWithInvalidFields")
    @Sql(value = "/sql/init_tarefa_with_2_tarefas.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void patchTarefa_whenInvalidFieldsOnBodyRequest_shouldReturn404BadRequest(String requestBodyFilename) {
        String uri = BASE_URL + "/1";
        String requestBody = fileUtils.readResourceFile("/tarefas/%s".formatted(requestBodyFilename));
        HttpEntity<String> request = buildHttpEntity(requestBody);

        ResponseEntity<ApiErrorResponse> response = restTemplate.exchange(uri, HttpMethod.PATCH, request, ApiErrorResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @ParameterizedTest
    @MethodSource("gerarPatchValidRequestAndExpectedResponse")
    @Sql(value = "/sql/init_tarefa_with_2_tarefas.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void patchTarefa_whenValidRequest_shouldSaveTarefaCorrectlyAndReturn204NoContent(String requestBodyFilename, Tarefa expectedSaved) {
        String uri = BASE_URL + "/2";
        String requestBody = fileUtils.readResourceFile("/tarefas/%s".formatted(requestBodyFilename));
        HttpEntity<String> request = buildHttpEntity(requestBody);

        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PATCH, request, Void.class);

        ArgumentCaptor<Tarefa> captor = ArgumentCaptor.forClass(Tarefa.class);
        BDDMockito.verify(repository, BDDMockito.times(1)).save(captor.capture());

        Tarefa saved = captor.getValue();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Assertions.assertThat(saved.getId()).isEqualTo(2L);
        Assertions.assertThat(saved.getNome()).isEqualTo(expectedSaved.getNome());
        Assertions.assertThat(saved.getDescricao()).isEqualTo(expectedSaved.getDescricao());
        Assertions.assertThat(saved.getRealizado()).isEqualTo(expectedSaved.getRealizado());
        Assertions.assertThat(saved.getPrioridade()).isEqualTo(expectedSaved.getPrioridade());


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

    public static Stream<Arguments> gerarPatchWithInvalidFields() {
        return Stream.of(
                Arguments.of("patch-tarefa-prioridade-invalida-request.json"),
                Arguments.of("patch-tarefa-nome-invalido-request.json"),
                Arguments.of("patch-tarefa-descricao-invalida-request.json"),
                Arguments.of("patch-tarefa-tudo-invalido-request.json")
        );
    }

    public static Stream<Arguments> gerarPatchValidRequestAndExpectedResponse() {
        return Stream.of(
                Arguments.of("post-tarefa-tudo-null-request.json", Tarefa.builder()
                        .id(2L)
                        .nome("Tomar cafe")
                        .descricao("Devo tomar algumas xicaras de cafe durante o dia.")
                        .realizado(true)
                        .prioridade(2)
                        .build()
                ),
                Arguments.of("patch-tarefa-204-request.json", Tarefa.builder()
                        .id(2L)
                        .nome("Estudar programacao")
                        .descricao("Devo estudar algumas horas por dia.")
                        .realizado(true)
                        .prioridade(3)
                        .build()
                )
        );
    }
}
