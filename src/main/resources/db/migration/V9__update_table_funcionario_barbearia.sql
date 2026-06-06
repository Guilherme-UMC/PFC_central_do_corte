ALTER TABLE funcionarios_barbearia
    ADD COLUMN disponivel BOOLEAN DEFAULT TRUE,
ADD COLUMN atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

CREATE OR REPLACE FUNCTION update_atualizado_em_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_funcionarios_barbearia_atualizado_em
    BEFORE UPDATE ON funcionarios_barbearia
    FOR EACH ROW EXECUTE FUNCTION update_atualizado_em_column();