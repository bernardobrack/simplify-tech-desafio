package brack.bernardo.simplify_tech_desafio.model;


import jakarta.persistence.*;

@Entity
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String descricao;
    @Column(nullable = false)
    private Boolean realizado;
    @Column(nullable = false)
    private Integer prioridade;
}
