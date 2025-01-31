package brack.bernardo.simplify_tech_desafio.request;

import lombok.*;

@Getter
@Setter
@Builder
public class PatchTarefaRequest {
    private String nome;
    private String descricao;
    private Integer prioridade;
    private Boolean realizado;
}
