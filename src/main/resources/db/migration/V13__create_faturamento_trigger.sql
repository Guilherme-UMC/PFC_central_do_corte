-- V13__create_faturamento_trigger.sql
-- Triggers para atualizar faturamento mensal automaticamente

-- Trigger para INSERT
CREATE OR REPLACE FUNCTION func_after_agendamento_insert()
RETURNS TRIGGER AS $$
DECLARE
ano_agendamento INT;
    mes_agendamento INT;
    valor_servico DECIMAL(10,2);
BEGIN
    ano_agendamento := EXTRACT(YEAR FROM NEW.data_hora);
    mes_agendamento := EXTRACT(MONTH FROM NEW.data_hora);

    -- Buscar o preço do serviço (usando 'servicos' no plural)
SELECT COALESCE(preco, 0) INTO valor_servico FROM servicos WHERE id = NEW.servico_id;

-- Só contabiliza se o status for CONCLUIDO
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

-- Trigger para UPDATE
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

    -- Buscar os preços dos serviços
SELECT COALESCE(preco, 0) INTO valor_servico_novo FROM servicos WHERE id = NEW.servico_id;
SELECT COALESCE(preco, 0) INTO valor_servico_antigo FROM servicos WHERE id = OLD.servico_id;

-- CASO 1: Status mudou para CONCLUIDO (antes não era)
IF NEW.status = 'CONCLUIDO' AND OLD.status != 'CONCLUIDO' THEN
        INSERT INTO faturamento_mensal (barbearia_id, ano, mes, valor_total, total_agendamentos)
        VALUES (NEW.barbearia_id, ano_agendamento, mes_agendamento, valor_servico_novo, 1)
        ON CONFLICT (barbearia_id, ano, mes) DO UPDATE SET
    valor_total = faturamento_mensal.valor_total + EXCLUDED.valor_total,
                                                       total_agendamentos = faturamento_mensal.total_agendamentos + EXCLUDED.total_agendamentos;

-- CASO 2: Status mudou de CONCLUIDO para outro (cancelamento após concluído)
ELSEIF OLD.status = 'CONCLUIDO' AND NEW.status != 'CONCLUIDO' THEN
        INSERT INTO faturamento_mensal (barbearia_id, ano, mes, valor_total, total_agendamentos)
        VALUES (NEW.barbearia_id, ano_agendamento, mes_agendamento, -valor_servico_antigo, -1)
        ON CONFLICT (barbearia_id, ano, mes) DO UPDATE SET
    valor_total = faturamento_mensal.valor_total + EXCLUDED.valor_total,
                                                           total_agendamentos = faturamento_mensal.total_agendamentos + EXCLUDED.total_agendamentos;

-- CASO 3: Serviço mudou mas continua CONCLUIDO
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

-- Trigger para DELETE
CREATE OR REPLACE FUNCTION func_after_agendamento_delete()
RETURNS TRIGGER AS $$
DECLARE
ano_agendamento INT;
    mes_agendamento INT;
    valor_servico DECIMAL(10,2);
BEGIN
    ano_agendamento := EXTRACT(YEAR FROM OLD.data_hora);
    mes_agendamento := EXTRACT(MONTH FROM OLD.data_hora);

    -- Buscar o preço do serviço
SELECT COALESCE(preco, 0) INTO valor_servico FROM servicos WHERE id = OLD.servico_id;

-- Só desconta se o status era CONCLUIDO
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