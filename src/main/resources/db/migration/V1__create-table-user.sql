CREATE TABLE users (
                       id VARCHAR(36) PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       telefone VARCHAR(20),
                       role VARCHAR(50) NOT NULL DEFAULT 'ROLE_CLIENTE',
                       active BOOLEAN DEFAULT true
);