package brack.bernardo.simplify_tech_desafio.service;


import brack.bernardo.simplify_tech_desafio.model.Tarefa;
import brack.bernardo.simplify_tech_desafio.repository.TarefaRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

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
    public void buscarPorId_whenIdGivenIsNull_shouldThrowBadRequestException() {
        Assertions.assertThatThrownBy(() -> service.buscarPorId(null))
                .isInstanceOf(ResponseStatusException.class)
                .message().contains("400 BAD_REQUEST");

    }

    @Test
    public void buscarPorId_whenIdGivenIsNotFound_shouldThrowNotFoundException() {
        BDDMockito.when(repository.findById(BDDMockito.anyLong())).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> service.buscarPorId(-1L))
                .isInstanceOf(ResponseStatusException.class)
                .message().contains("404 NOT_FOUND");
    }

    @Test
    public void buscarPorId_whenIdGivenIsFound_shouldReturnTarefa() {
        BDDMockito.when(repository.findById(BDDMockito.anyLong())).thenReturn(Optional.of(tarefa));
        tarefa.setId(1L);
        Tarefa actual = service.buscarPorId(tarefa.getId());
        BDDMockito.verify(repository, BDDMockito.times(1)).findById(tarefa.getId());
        Assertions.assertThat(actual).isEqualTo(tarefa);
    }

    @Test
    public void salvar_whenTarefaIsNull_shouldThrowBadRequestException() {
        Assertions.assertThatThrownBy(() -> service.salvar(null))
                .isInstanceOf(ResponseStatusException.class)
                .message().contains("400 BAD_REQUEST");
    }

    @Test
    public void salvar_whenTarefaIsFullyGiven_shouldCallRepositorySaveWithGivenTarefa() {
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
    public void salvar_whenTarefaRealizadoIsNull_shouldCallRepositorySaveWithRealizadoFalse() {
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
    public void salvar_whenTarefaPriorityIsNullOrInvalid_shouldCallRepositorySaveWithPriorityZero(Integer priority) {
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
    public void salvar_whenTarefaIsGiven_shouldReturnIt() {
        BDDMockito.when(repository.save(BDDMockito.any(Tarefa.class))).thenReturn(tarefa);
        Tarefa actual = service.salvar(tarefa);
        Assertions.assertThat(actual.getNome()).isEqualTo(tarefa.getNome());
        Assertions.assertThat(actual.getDescricao()).isEqualTo(tarefa.getDescricao());
        Assertions.assertThat(actual.getPrioridade()).isEqualTo(tarefa.getPrioridade());
        Assertions.assertThat(actual.getRealizado()).isEqualTo(tarefa.getRealizado());
    }

    private static Stream<Arguments> invalidPrioritiesSource() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(-1),
                Arguments.of(4)
        );
    }
}
