DO $$
DECLARE
barbearia_record RECORD;
BEGIN
FOR barbearia_record IN SELECT id FROM barbearias LOOP

IF NOT EXISTS (SELECT 1 FROM horario_funcionamento WHERE barbearia_id = barbearia_record.id) THEN

INSERT INTO horario_funcionamento (barbearia_id, dia, hora_abertura, hora_fechamento, fechado, created_at, updated_at)
VALUES
    (barbearia_record.id, 'SEGUNDA', '09:00:00', '18:00:00', false, NOW(), NOW()),
    (barbearia_record.id, 'TERCA', '09:00:00', '18:00:00', false, NOW(), NOW()),
    (barbearia_record.id, 'QUARTA', '09:00:00', '18:00:00', false, NOW(), NOW()),
    (barbearia_record.id, 'QUINTA', '09:00:00', '18:00:00', false, NOW(), NOW()),
    (barbearia_record.id, 'SEXTA', '09:00:00', '18:00:00', false, NOW(), NOW()),
    (barbearia_record.id, 'SABADO', '09:00:00', '18:00:00', false, NOW(), NOW()),
    (barbearia_record.id, 'DOMINGO', '09:00:00', '18:00:00', false, NOW(), NOW());
END IF;
END LOOP;
END $$;