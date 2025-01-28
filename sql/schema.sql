CREATE TABLE IF NOT EXISTS tarefa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome varchar(255) NOT NULL,
    descricao varchar(255) NOT NULL,
    realizado boolean DEFAULT false,
    prioridade int DEFAULT 0
);