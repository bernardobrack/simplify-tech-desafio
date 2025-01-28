CREATE TABLE IF NOT EXISTS Tarefa (
    id BIGINT PRIMARY KEY,
    nome varchar(255) NOT NULL,
    descricao varchar(255) NOT NULL,
    realizado boolean NOT NULL DEFAULT false,
    priority int NOT NULL DEFAULT 0
);