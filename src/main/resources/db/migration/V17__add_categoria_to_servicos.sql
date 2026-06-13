ALTER TABLE servicos ADD COLUMN categoria VARCHAR(50);

UPDATE servicos SET categoria = 'OUTROS' WHERE categoria IS NULL;

ALTER TABLE servicos ALTER COLUMN categoria SET NOT NULL;

CREATE INDEX idx_servicos_categoria ON servicos(categoria);