
CREATE OR REPLACE FUNCTION func_after_agendamento_insert()
RETURNS TRIGGER AS $$
DECLARE
ano_agendamento INT;
    mes_agendamento INT;
    valor_servico DECIMAL(10,2);
BEGIN
    ano_agendamento := EXTRACT(YEAR FROM NEW.data_hora);
    mes_agendamento := EXTRACT(MONTH FROM NEW.data_hora);

SELECT COALESCE(preco, 0) INTO valor_servico FROM servicos WHERE id = NEW.servico_id;

IF NEW.status = 'CONCLUIDO' THEN
        INSERT INTO faturamento_mensal (barbearia_id, ano, mes, valor_total, total_agendamentos)
        VALUES (NEW.barbearia_id, ano_agendamento, mes_agendamento, valor_servico, 1)
        ON CONFLICT (barbearia_id, ano, mes) DO UPDATE SET
    valor_total = faturamento_mensal.valor_total + EXCLUDED.valor_total,
                                                       total_agendamentos = faturamento_mensal.total_agendamentos + EXCLUDED.total_agendamentos;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS after_agendamento_insert ON agendamento;
CREATE TRIGGER after_agendamento_insert
    AFTER INSERT ON agendamento
    FOR EACH ROW
    EXECUTE FUNCTION func_after_agendamento_insert();

CREATE OR REPLACE FUNCTION func_after_agendamento_update()
RETURNS TRIGGER AS $$
DECLARE
ano_agendamento INT;
    mes_agendamento INT;
    valor_servico_novo DECIMAL(10,2);
    valor_servico_antigo DECIMAL(10,2);
BEGIN
    ano_agendamento := EXTRACT(YEAR FROM NEW.data_hora);
    mes_agendamento := EXTRACT(MONTH FROM NEW.data_hora);

SELECT COALESCE(preco, 0) INTO valor_servico_novo FROM servicos WHERE id = NEW.servico_id;
SELECT COALESCE(preco, 0) INTO valor_servico_antigo FROM servicos WHERE id = OLD.servico_id;

IF NEW.status = 'CONCLUIDO' AND OLD.status != 'CONCLUIDO' THEN
        INSERT INTO faturamento_mensal (barbearia_id, ano, mes, valor_total, total_agendamentos)
        VALUES (NEW.barbearia_id, ano_agendamento, mes_agendamento, valor_servico_novo, 1)
        ON CONFLICT (barbearia_id, ano, mes) DO UPDATE SET
    valor_total = faturamento_mensal.valor_total + EXCLUDED.valor_total,
    total_agendamentos = faturamento_mensal.total_agendamentos + EXCLUDED.total_agendamentos;

ELSEIF OLD.status = 'CONCLUIDO' AND NEW.status != 'CONCLUIDO' THEN
        INSERT INTO faturamento_mensal (barbearia_id, ano, mes, valor_total, total_agendamentos)
        VALUES (NEW.barbearia_id, ano_agendamento, mes_agendamento, -valor_servico_antigo, -1)
        ON CONFLICT (barbearia_id, ano, mes) DO UPDATE SET
    valor_total = faturamento_mensal.valor_total + EXCLUDED.valor_total,
    total_agendamentos = faturamento_mensal.total_agendamentos + EXCLUDED.total_agendamentos;

ELSEIF OLD.status = 'CONCLUIDO' AND NEW.status = 'CONCLUIDO' AND OLD.servico_id != NEW.servico_id THEN
        INSERT INTO faturamento_mensal (barbearia_id, ano, mes, valor_total, total_agendamentos)
        VALUES (NEW.barbearia_id, ano_agendamento, mes_agendamento, valor_servico_novo - valor_servico_antigo, 0)
        ON CONFLICT (barbearia_id, ano, mes) DO UPDATE SET
    valor_total = faturamento_mensal.valor_total + EXCLUDED.valor_total;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS after_agendamento_update ON agendamento;
CREATE TRIGGER after_agendamento_update
    AFTER UPDATE ON agendamento
    FOR EACH ROW
    EXECUTE FUNCTION func_after_agendamento_update();

CREATE OR REPLACE FUNCTION func_after_agendamento_delete()
RETURNS TRIGGER AS $$
DECLARE
ano_agendamento INT;
    mes_agendamento INT;
    valor_servico DECIMAL(10,2);
BEGIN
    ano_agendamento := EXTRACT(YEAR FROM OLD.data_hora);
    mes_agendamento := EXTRACT(MONTH FROM OLD.data_hora);

SELECT COALESCE(preco, 0) INTO valor_servico FROM servicos WHERE id = OLD.servico_id;

IF OLD.status = 'CONCLUIDO' THEN
        INSERT INTO faturamento_mensal (barbearia_id, ano, mes, valor_total, total_agendamentos)
        VALUES (OLD.barbearia_id, ano_agendamento, mes_agendamento, -valor_servico, -1)
        ON CONFLICT (barbearia_id, ano, mes) DO UPDATE SET
    valor_total = faturamento_mensal.valor_total + EXCLUDED.valor_total,
    total_agendamentos = faturamento_mensal.total_agendamentos + EXCLUDED.total_agendamentos;
END IF;

RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS after_agendamento_delete ON agendamento;
CREATE TRIGGER after_agendamento_delete
    AFTER DELETE ON agendamento
    FOR EACH ROW
    EXECUTE FUNCTION func_after_agendamento_delete();