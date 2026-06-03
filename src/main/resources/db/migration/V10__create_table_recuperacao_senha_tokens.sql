CREATE TABLE IF NOT EXISTS recuperacao_senha_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    usuario_id VARCHAR(36) NOT NULL,
    data_expiracao TIMESTAMP NOT NULL,
    utilizado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_recuperacao_token_usuario
    FOREIGN KEY (usuario_id)
    REFERENCES usuarios(id)
    ON DELETE CASCADE
    );

CREATE INDEX idx_recuperacao_token_token ON recuperacao_senha_tokens(token);
CREATE INDEX idx_recuperacao_token_usuario ON recuperacao_senha_tokens(usuario_id);
CREATE INDEX idx_recuperacao_token_expiracao ON recuperacao_senha_tokens(data_expiracao);