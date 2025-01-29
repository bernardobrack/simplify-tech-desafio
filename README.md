# Desafio sistema de gerenciamento de tarefas (To-Do List)
Repositório feito para resolver o desafio proposto neste README.

## Descrição
- Desenvolva uma aplicação web utilizando uma linguagem de programação e um framework de sua escolha. A aplicação deve consistir em um sistema de gerenciamento de tarefas, onde os usuários podem criar, visualizar, editar e excluir tarefas.

## Requisitos
- Usar banco de dados
- Campos mínimos da entidade de tarefa
    - Nome
    - Descrição
    - Realizado
    - Prioridade
- Criar CRUD de tarefas

## Considerações sobre a solução
- Foi escolhida a linguagem Java, com Spring Boot para fazer uma API Rest.
- Foi utilizado TestRestTemplate para as requisições em testes de integração.
- O banco de dados utilizado foi o MySql (iniciar através de 'docker compose up' na root do projeto).
- Para os testes, foi utilizado Testcontainers com um container MySql.
- A criação das tabelas é feita por um arquivo schema.sql, o Data JPA ddl-auto está em NONE.
- As variáveis de ambiente estão em arquivos .env não upados no GitHub, utilizar os .envTemplate para criação.
- Foi utilizado o maven-surefire-plugin para rodar os testes com o Spring Profile "test" automaticamente.