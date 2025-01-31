package brack.bernardo.simplify_tech_desafio.mapper;

import brack.bernardo.simplify_tech_desafio.model.Tarefa;
import brack.bernardo.simplify_tech_desafio.request.PatchTarefaRequest;
import brack.bernardo.simplify_tech_desafio.request.PostTarefaRequest;
import brack.bernardo.simplify_tech_desafio.response.GetTarefaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TarefaMapper {

    GetTarefaResponse toGetTarefaResponse(Tarefa tarefa);
    List<GetTarefaResponse> toGetTarefaResponseList(List<Tarefa> tarefa);


    Tarefa toTarefa(PostTarefaRequest tarefa);
    Tarefa toTarefa(PatchTarefaRequest tarefa);

}
