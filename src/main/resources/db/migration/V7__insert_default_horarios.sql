DO $$
DECLARE
barbearia_record RECORD;
    dia_atual TEXT;
BEGIN
FOR barbearia_record IN SELECT id FROM barbearias LOOP

    FOR dia_atual IN
SELECT unnest(ARRAY['SEGUNDA', 'TERCA', 'QUARTA', 'QUINTA', 'SEXTA']) AS dia
    LOOP
    INSERT INTO horario_funcionamento (barbearia_id, dia, hora_abertura, hora_fechamento, fechado)
VALUES (barbearia_record.id, dia_atual, '09:00:00', '18:00:00', FALSE)
ON CONFLICT (barbearia_id, dia) DO NOTHING;
END LOOP;


INSERT INTO horario_funcionamento (barbearia_id, dia, hora_abertura, hora_fechamento, fechado)
VALUES (barbearia_record.id, 'SABADO', '09:00:00', '14:00:00', FALSE)
    ON CONFLICT (barbearia_id, dia) DO NOTHING;


INSERT INTO horario_funcionamento (barbearia_id, dia, fechado)
VALUES (barbearia_record.id, 'DOMINGO', TRUE)
    ON CONFLICT (barbearia_id, dia) DO NOTHING;
END LOOP;
END $$;