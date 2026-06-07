-- V12__create_faturamento_mensal_table.sql
-- Tabela de resumo de faturamento mensal (apenas agendamentos CONCLUÍDOS)

-- Criar tabela
CREATE TABLE IF NOT EXISTS faturamento_mensal (
                                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    barbearia_id VARCHAR(36) NOT NULL,
    ano INT NOT NULL,
    mes INT NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_agendamentos INT NOT NULL DEFAULT 0,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_barbearia_ano_mes UNIQUE (barbearia_id, ano, mes)
    );

-- Criar trigger para atualizar o updated_at automaticamente
CREATE OR REPLACE FUNCTION update_faturamento_atualizado_em()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_faturamento_mensal_atualizado_em
    BEFORE UPDATE ON faturamento_mensal
    FOR EACH ROW
    EXECUTE FUNCTION update_faturamento_atualizado_em();

-- Índices para performance
CREATE INDEX idx_faturamento_barbearia ON faturamento_mensal(barbearia_id);
CREATE INDEX idx_faturamento_data ON faturamento_mensal(ano, mes);