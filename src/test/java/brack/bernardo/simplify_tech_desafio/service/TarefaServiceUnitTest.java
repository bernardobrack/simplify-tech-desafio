package brack.bernardo.simplify_tech_desafio.service;


import brack.bernardo.simplify_tech_desafio.exception.ContentNotFoundException;
import brack.bernardo.simplify_tech_desafio.exception.MalformedContentException;
import brack.bernardo.simplify_tech_desafio.model.Tarefa;
import brack.bernardo.simplify_tech_desafio.repository.TarefaRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class TarefaServiceUnitTest {

    @InjectMocks
    private TarefaService service;

    @Mock
    private TarefaRepository repository;

    private Tarefa tarefa;


    @BeforeEach
    void setUp() {
        tarefa = Tarefa.builder()
                .id(null)
                .nome("Arrumar a cama")
                .descricao("Tenho que arrumar a cama todos os dias.")
                .realizado(false)
                .prioridade(3)
                .build();
    }


    @Test
    void buscarPorId_whenIdGivenIsNull_shouldThrowMalformedContentException() {
        Assertions.assertThatThrownBy(() -> service.buscarPorId(null))
                .isInstanceOf(MalformedContentException.class);

    }

    @Test
    void buscarPorId_whenIdGivenIsNotFound_shouldThrowNotFoundException() {
        BDDMockito.when(repository.findById(BDDMockito.anyLong())).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> service.buscarPorId(-1L))
                .isInstanceOf(ContentNotFoundException.class);
    }

    @Test
    void buscarPorId_whenIdGivenIsFound_shouldReturnTarefa() {
        BDDMockito.when(repository.findById(BDDMockito.anyLong())).thenReturn(Optional.of(tarefa));
        tarefa.setId(1L);
        Tarefa actual = service.buscarPorId(tarefa.getId());
        BDDMockito.verify(repository, BDDMockito.times(1)).findById(tarefa.getId());
        Assertions.assertThat(actual).isEqualTo(tarefa);
    }

    @Test
    void salvar_whenTarefaIsNull_shouldThrowMalformedContentException() {
        Assertions.assertThatThrownBy(() -> service.salvar(null))
                .isInstanceOf(MalformedContentException.class);
    }

    @Test
    void salvar_whenTarefaIsFullyGiven_shouldCallRepositorySaveWithGivenTarefa() {
        ArgumentCaptor<Tarefa> captor = ArgumentCaptor.forClass(Tarefa.class);
        service.salvar(tarefa);
        BDDMockito.verify(repository, BDDMockito.times(1)).save(captor.capture());

        Tarefa salva = captor.getValue();
        Assertions.assertThat(salva.getNome()).isEqualTo(tarefa.getNome());
        Assertions.assertThat(salva.getDescricao()).isEqualTo(tarefa.getDescricao());
        Assertions.assertThat(salva.getRealizado()).isEqualTo(tarefa.getRealizado());
        Assertions.assertThat(salva.getPrioridade()).isEqualTo(tarefa.getPrioridade());
    }

    @Test
    void salvar_whenTarefaRealizadoIsNull_shouldCallRepositorySaveWithRealizadoFalse() {
        ArgumentCaptor<Tarefa> captor = ArgumentCaptor.forClass(Tarefa.class);
        tarefa.setRealizado(null);
        service.salvar(tarefa);
        BDDMockito.verify(repository, BDDMockito.times(1)).save(captor.capture());

        Tarefa salva = captor.getValue();
        Assertions.assertThat(salva.getNome()).isEqualTo(tarefa.getNome());
        Assertions.assertThat(salva.getDescricao()).isEqualTo(tarefa.getDescricao());
        Assertions.assertThat(salva.getRealizado()).isFalse();
        Assertions.assertThat(salva.getPrioridade()).isEqualTo(tarefa.getPrioridade());
    }

    @ParameterizedTest
    @MethodSource(value = "invalidPrioritiesSource")
    void salvar_whenTarefaPriorityIsNullOrInvalid_shouldCallRepositorySaveWithPriorityZero(Integer priority) {
        ArgumentCaptor<Tarefa> captor = ArgumentCaptor.forClass(Tarefa.class);
        tarefa.setPrioridade(priority);
        service.salvar(tarefa);
        BDDMockito.verify(repository, BDDMockito.times(1)).save(captor.capture());

        Tarefa salva = captor.getValue();
        Assertions.assertThat(salva.getNome()).isEqualTo(tarefa.getNome());
        Assertions.assertThat(salva.getDescricao()).isEqualTo(tarefa.getDescricao());
        Assertions.assertThat(salva.getRealizado()).isEqualTo(tarefa.getRealizado());
        Assertions.assertThat(salva.getPrioridade()).isZero();
    }

    @Test
    void salvar_whenTarefaIsGiven_shouldReturnIt() {
        BDDMockito.when(repository.save(BDDMockito.any(Tarefa.class))).thenReturn(tarefa);
        Tarefa actual = service.salvar(tarefa);
        Assertions.assertThat(actual.getNome()).isEqualTo(tarefa.getNome());
        Assertions.assertThat(actual.getDescricao()).isEqualTo(tarefa.getDescricao());
        Assertions.assertThat(actual.getPrioridade()).isEqualTo(tarefa.getPrioridade());
        Assertions.assertThat(actual.getRealizado()).isEqualTo(tarefa.getRealizado());
    }

    @ParameterizedTest
    @MethodSource("gerarFindAllByNomeLikeRealizadoAndPrioridadeAceitandoNullParametros")
    void listar_mustCallRepositoryFindAllByNomeLikeRealizadoAndPrioridadeAceitandoNullWithCorrectParams(String nome, Boolean realizado, Integer prioridade, Pageable pageable) {
        String filtro = nome == null ? "%" : "%" + nome + "%";
        service.listar(nome, realizado, prioridade, pageable);
        BDDMockito.verify(repository, BDDMockito.times(1)).findAllByNomeLikeRealizadoAndPrioridadeAceitandoNull(filtro, realizado, prioridade, pageable);
    }

    @Test
    void listar_mustReturnPageReturnedFromRepository() {
        Page<Tarefa> pageToReturn = new PageImpl<Tarefa>(List.of(tarefa));
        BDDMockito.when(repository.findAllByNomeLikeRealizadoAndPrioridadeAceitandoNull(BDDMockito.anyString(), BDDMockito.anyBoolean(), BDDMockito.anyInt(), BDDMockito.any(Pageable.class)))
                .thenReturn(pageToReturn);
        Page<Tarefa> returnedPage = service.listar("arr", false, 3, Pageable.unpaged());
        Assertions.assertThat(returnedPage).isNotNull().isEqualTo(pageToReturn);
    }

    @Test
    void atualizarTarefa_whenTarefaWithIdDoesntExist_shouldThrowContentNotFoundException() {
        BDDMockito.when(repository.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> service.atualizarTarefa(1L, tarefa))
                .isInstanceOf(ContentNotFoundException.class);
    }

    @Test
    void atualizarTarefa_whenTarefaWithIdExists_shouldCallRepositoryWithItsNewValues() {
        BDDMockito.when(repository.findById(1L)).thenReturn(Optional.of(tarefa));
        ArgumentCaptor<Tarefa> captor = ArgumentCaptor.forClass(Tarefa.class);
        Tarefa newTarefa = Tarefa.builder().nome("AAAA nome").build();

        service.atualizarTarefa(1L, newTarefa);

        BDDMockito.verify(repository, BDDMockito.times(1)).save(captor.capture());

        Tarefa salva = captor.getValue();
        Assertions.assertThat(salva).isSameAs(tarefa);
        Assertions.assertThat(salva).isNotSameAs(newTarefa);
        Assertions.assertThat(salva.getNome()).isEqualTo(newTarefa.getNome());
        Assertions.assertThat(salva.getDescricao()).isNotNull();
        Assertions.assertThat(salva.getPrioridade()).isNotNull();
        Assertions.assertThat(salva.getRealizado()).isNotNull();

    }

    @ParameterizedTest
    @MethodSource("gerarTarefaMalFormada")
    void atualizarTarefa_whenTarefaWithIdExistsButFieldInvalid_shouldThrowMalformedContentException(String nome, String descricao, Boolean realizado, Integer prioridade) {
        Tarefa newTarefa = Tarefa.builder().nome(nome).descricao(descricao).realizado(realizado).prioridade(prioridade).build();
        BDDMockito.when(repository.findById(1L)).thenReturn(Optional.of(tarefa));
        Assertions.assertThatThrownBy(() -> service.atualizarTarefa(1L, newTarefa))
                .isInstanceOf(MalformedContentException.class);
    }


    @ParameterizedTest
    @MethodSource("gerarTarefaBemFormada")
    void atualizarTarefa_whenTarefaWithIdExistsAndValid_shouldUpdateOnlyNonNullFields(String nome, String descricao, Boolean realizado, Integer prioridade) {
        Tarefa newTarefa = Tarefa.builder()
                .nome(nome)
                .descricao(descricao)
                .realizado(realizado)
                .prioridade(prioridade)
                .build();
        tarefa.setId(1L);
        BDDMockito.when(repository.findById(1L)).thenReturn(Optional.of(tarefa));
        ArgumentCaptor<Tarefa> captor = ArgumentCaptor.forClass(Tarefa.class);

        service.atualizarTarefa(1L, newTarefa);
        BDDMockito.verify(repository, BDDMockito.times(1)).save(captor.capture());

        Tarefa salva = captor.getValue();
        Assertions.assertThat(salva).hasNoNullFieldsOrProperties();
        if(nome != null) Assertions.assertThat(salva.getNome()).isEqualTo(nome);
        if(descricao != null) Assertions.assertThat(salva.getDescricao()).isEqualTo(descricao);
        if(realizado != null) Assertions.assertThat(salva.getRealizado()).isEqualTo(realizado);
        if(prioridade != null) Assertions.assertThat(salva.getPrioridade()).isEqualTo(prioridade);

    }

    public static Stream<Arguments> gerarFindAllByNomeLikeRealizadoAndPrioridadeAceitandoNullParametros() {
        return Stream.of(
                Arguments.of((Object) null, true, 0, Pageable.unpaged()),
                Arguments.of( "ca", false, 1, Pageable.unpaged()),
                Arguments.of( "bo", true, 2, Pageable.unpaged())

        );
    }
    private static Stream<Arguments> invalidPrioritiesSource() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(-1),
                Arguments.of(4)
        );
    }

    public static Stream<Arguments> gerarTarefaMalFormada() {
        return Stream.of(
                Arguments.of("", "descricao", null,null),
                Arguments.of("nome", "", true, 0),
                Arguments.of("nome", "descricao", null, 4),
                Arguments.of("nome", "descricao", null, -1)
        );
    }

    public static Stream<Arguments> gerarTarefaBemFormada() {
        return Stream.of(
                Arguments.of("nome",null,null,null),
                Arguments.of(null,"descricao",null,null),
                Arguments.of("nome","descricao",true,null),
                Arguments.of(null,null,null,null),
                Arguments.of("nome", "descricao", false, 1)
        );
    }


}
