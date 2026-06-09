INSERT INTO faturamento_mensal (barbearia_id, ano, mes, valor_total, total_agendamentos)
SELECT
    a.barbearia_id,
    EXTRACT(YEAR FROM a.data_hora)::INT as ano,
    EXTRACT(MONTH FROM a.data_hora)::INT as mes,
    COALESCE(SUM(s.preco), 0) as valor_total,
    COUNT(*) as total_agendamentos
FROM agendamento a
         INNER JOIN servicos s ON a.servico_id = s.id
WHERE a.status = 'CONCLUIDO'
GROUP BY a.barbearia_id, EXTRACT(YEAR FROM a.data_hora), EXTRACT(MONTH FROM a.data_hora)
    ON CONFLICT (barbearia_id, ano, mes) DO UPDATE SET
    valor_total = EXCLUDED.valor_total,
    total_agendamentos = EXCLUDED.total_agendamentos,
    atualizado_em = CURRENT_TIMESTAMP;