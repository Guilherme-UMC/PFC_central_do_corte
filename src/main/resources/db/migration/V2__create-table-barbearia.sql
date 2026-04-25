CREATE TABLE barbearias (
    id VARCHAR(36) PRIMARY KEY,
    owner_id VARCHAR(36) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    logradouro VARCHAR(150) NOT NULL,
    numero VARCHAR(10) NOT NULL,
    bairro VARCHAR(100) NOT NULL,
    cep VARCHAR(10) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    img_url VARCHAR(500),
    telefone VARCHAR(20) NOT NULL,
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_barbearia_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_barbearia_owner ON barbearias(owner_id);
CREATE INDEX idx_barbearia_cidade_uf ON barbearias(cidade, uf);