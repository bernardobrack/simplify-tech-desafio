package brack.bernardo.simplify_tech_desafio.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostTarefaRequest {

    @NotBlank
    private String nome;

    @NotBlank
    private String descricao;


    private Boolean realizado;


    private Integer prioridade;

}
