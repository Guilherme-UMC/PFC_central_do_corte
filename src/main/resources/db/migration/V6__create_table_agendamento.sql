CREATE TABLE agendamento (
 id BIGSERIAL PRIMARY KEY,
 barbearia_id VARCHAR(36) NOT NULL,
 cliente_id VARCHAR(36) NOT NULL,
 servico_id VARCHAR(36) NOT NULL,
 funcionario_id VARCHAR(36),
 data_hora TIMESTAMP NOT NULL,
 status VARCHAR(30) NOT NULL DEFAULT 'PENDENTE',
 observacao TEXT,
 criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

 CONSTRAINT fk_agendamento_barbearia FOREIGN KEY (barbearia_id) REFERENCES barbearias(id) ON DELETE CASCADE,
 CONSTRAINT fk_agendamento_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios(id) ON DELETE CASCADE,
 CONSTRAINT fk_agendamento_servico FOREIGN KEY (servico_id) REFERENCES servicos(id) ON DELETE CASCADE,
 CONSTRAINT fk_agendamento_funcionario FOREIGN KEY (funcionario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);


CREATE INDEX idx_agendamento_barbearia_data ON agendamento(barbearia_id, data_hora);
CREATE INDEX idx_agendamento_cliente ON agendamento(cliente_id);
CREATE INDEX idx_agendamento_status ON agendamento(status);
CREATE INDEX idx_agendamento_data_hora ON agendamento(data_hora);
CREATE INDEX idx_agendamento_funcionario ON agendamento(funcionario_id);