ALTER TABLE usuarios ADD COLUMN email_confirmado BOOLEAN DEFAULT FALSE;

CREATE TABLE email_confirmacao_tokens (
                                          id VARCHAR(255) PRIMARY KEY,
                                          token VARCHAR(255) NOT NULL UNIQUE,
                                          usuario_id VARCHAR(255) NOT NULL,
                                          data_expiracao TIMESTAMP NOT NULL,
                                          utilizado BOOLEAN NOT NULL DEFAULT FALSE,
                                          CONSTRAINT fk_email_confirmacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE INDEX idx_email_confirmacao_token ON email_confirmacao_tokens(token);
CREATE INDEX idx_email_confirmacao_usuario_id ON email_confirmacao_tokens(usuario_id);