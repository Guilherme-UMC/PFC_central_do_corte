CREATE TABLE IF NOT EXISTS produtos (
                                        id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    barbearia_id VARCHAR(36) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    preco DECIMAL(10, 2),
    imagem_url VARCHAR(500),
    categoria VARCHAR(50),
    marca VARCHAR(100),
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_produtos_barbearia ON produtos(barbearia_id);
CREATE INDEX IF NOT EXISTS idx_produtos_ativo ON produtos(ativo);
CREATE INDEX IF NOT EXISTS idx_produtos_categoria ON produtos(categoria);

ALTER TABLE produtos
    ADD CONSTRAINT fk_produtos_barbearia
        FOREIGN KEY (barbearia_id) REFERENCES barbearias(id) ON DELETE CASCADE;