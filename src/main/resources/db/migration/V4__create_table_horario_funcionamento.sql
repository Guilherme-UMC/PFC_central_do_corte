CREATE TABLE horario_funcionamento (
   id BIGSERIAL PRIMARY KEY,
   barbearia_id VARCHAR(36) NOT NULL,
   dia VARCHAR(20) NOT NULL,
   hora_abertura TIME,
   hora_fechamento TIME,
   fechado BOOLEAN DEFAULT FALSE,
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

   CONSTRAINT fk_horario_barbearia FOREIGN KEY (barbearia_id) REFERENCES barbearias(id) ON DELETE CASCADE,
   CONSTRAINT unique_barbearia_dia UNIQUE(barbearia_id, dia)
);


CREATE INDEX idx_horario_barbearia ON horario_funcionamento(barbearia_id);