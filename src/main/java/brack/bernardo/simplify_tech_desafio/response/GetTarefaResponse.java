package brack.bernardo.simplify_tech_desafio.response;

public record GetTarefaResponse(
        Long id,
        String nome,
        String descricao,
        Boolean realizado,
        Integer prioridade
) {
}
