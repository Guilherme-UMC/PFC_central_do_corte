CREATE TABLE IF NOT EXISTS logs_sistema (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(50) NOT NULL,
    acao VARCHAR(50) NOT NULL,
    usuario_id VARCHAR(36) NOT NULL,
    usuario_email VARCHAR(255),
    usuario_nome VARCHAR(255),
    usuario_role VARCHAR(50),
    entidade VARCHAR(100),
    entidade_id VARCHAR(36),
    descricao VARCHAR(1000),
    detalhes TEXT,
    ip_origem VARCHAR(45),
    user_agent TEXT,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_logs_tipo ON logs_sistema(tipo);
CREATE INDEX idx_logs_usuario ON logs_sistema(usuario_id);
CREATE INDEX idx_logs_data ON logs_sistema(data_hora);
CREATE INDEX idx_logs_entidade ON logs_sistema(entidade);
CREATE INDEX idx_logs_entidade_id ON logs_sistema(entidade_id);

COMMENT ON TABLE logs_sistema IS 'Registro de todas as ações do sistema';
COMMENT ON COLUMN logs_sistema.tipo IS 'Tipo da ação: AGENDAMENTO, USUARIO, BARBEARIA, SERVICO, FUNCIONARIO, LOGIN, LOGOUT';
COMMENT ON COLUMN logs_sistema.acao IS 'Ação executada: CRIADO, ATUALIZADO, CANCELADO, CONFIRMADO, CONCLUIDO, DELETADO, LOGIN, LOGOUT';