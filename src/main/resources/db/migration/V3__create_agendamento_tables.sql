-- Tabela de serviços
CREATE TABLE servicos (
id VARCHAR(36) PRIMARY KEY,
nome VARCHAR(100) NOT NULL,
descricao TEXT,
preco DECIMAL(10,2) NOT NULL,
duracao_minutos INT NOT NULL,
ativo BOOLEAN DEFAULT TRUE,
barbearia_id VARCHAR(36) NOT NULL,
FOREIGN KEY (barbearia_id) REFERENCES barbearias(id)
);

-- Tabela de horários de funcionamento
CREATE TABLE horarios_funcionamento (
id VARCHAR(36) PRIMARY KEY,
dia_semana INT NOT NULL,
hora_inicio TIME NOT NULL,
hora_fim TIME NOT NULL,
ativo BOOLEAN DEFAULT TRUE,
barbearia_id VARCHAR(36) NOT NULL,
FOREIGN KEY (barbearia_id) REFERENCES barbearias(id)
);

-- Tabela de vínculo funcionário-barbearia
CREATE TABLE funcionarios_barbearia (
id VARCHAR(36) PRIMARY KEY,
funcionario_id VARCHAR(36) NOT NULL,
barbearia_id VARCHAR(36) NOT NULL,
ativo BOOLEAN DEFAULT TRUE,
criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (funcionario_id) REFERENCES users(id),
FOREIGN KEY (barbearia_id) REFERENCES barbearias(id),
UNIQUE (funcionario_id, barbearia_id)
);

-- Tabela de agendamentos
CREATE TABLE agendamentos (
id VARCHAR(36) PRIMARY KEY,
data_hora TIMESTAMP NOT NULL,
observacoes TEXT,
status VARCHAR(20) NOT NULL,
criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
cliente_id VARCHAR(36) NOT NULL,
barbearia_id VARCHAR(36) NOT NULL,
funcionario_id VARCHAR(36) NOT NULL,
FOREIGN KEY (cliente_id) REFERENCES users(id),
FOREIGN KEY (barbearia_id) REFERENCES barbearias(id),
FOREIGN KEY (funcionario_id) REFERENCES users(id)
);

-- Tabela de relacionamento agendamento-serviços
CREATE TABLE agendamento_servicos (
agendamento_id VARCHAR(36) NOT NULL,
servico_id VARCHAR(36) NOT NULL,
PRIMARY KEY (agendamento_id, servico_id),
FOREIGN KEY (agendamento_id) REFERENCES agendamentos(id),
FOREIGN KEY (servico_id) REFERENCES servicos(id)
);

-- Índices para performance
CREATE INDEX idx_agendamentos_data_hora ON agendamentos(data_hora);
CREATE INDEX idx_agendamentos_cliente ON agendamentos(cliente_id);
CREATE INDEX idx_agendamentos_barbearia ON agendamentos(barbearia_id);
CREATE INDEX idx_agendamentos_funcionario ON agendamentos(funcionario_id);
CREATE INDEX idx_servicos_barbearia ON servicos(barbearia_id);