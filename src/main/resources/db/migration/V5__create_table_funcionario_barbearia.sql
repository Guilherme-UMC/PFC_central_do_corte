CREATE TABLE funcionarios_barbearia (

    id VARCHAR(36) PRIMARY KEY,
    funcionario_id VARCHAR(36) NOT NULL,
    barbearia_id VARCHAR(36) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_funcionario_barbearia_funcionario FOREIGN KEY (funcionario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_funcionario_barbearia_barbearia FOREIGN KEY (barbearia_id) REFERENCES barbearias(id) ON DELETE CASCADE,
    CONSTRAINT unique_funcionario_barbearia UNIQUE(funcionario_id, barbearia_id)
);


CREATE INDEX idx_funcionario_barbearia_funcionario ON funcionarios_barbearia(funcionario_id);
CREATE INDEX idx_funcionario_barbearia_barbearia ON funcionarios_barbearia(barbearia_id);