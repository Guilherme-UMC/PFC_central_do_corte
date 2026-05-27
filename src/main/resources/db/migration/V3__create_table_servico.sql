CREATE TABLE servicos (
  id VARCHAR(36) PRIMARY KEY,
  nome VARCHAR(100) NOT NULL,
  descricao TEXT,
  preco DECIMAL(10, 2) NOT NULL,
  duracao_minutos INT NOT NULL,
  ativo BOOLEAN DEFAULT TRUE,
  barbearia_id VARCHAR(36) NOT NULL,

  CONSTRAINT fk_servico_barbearia FOREIGN KEY (barbearia_id) REFERENCES barbearias(id) ON DELETE CASCADE
);


CREATE INDEX idx_servicos_barbearia ON servicos(barbearia_id);
CREATE INDEX idx_servicos_ativo ON servicos(ativo);