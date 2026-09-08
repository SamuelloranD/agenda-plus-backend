CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    senha_hash VARCHAR(60) NOT NULL,
    role VARCHAR(20) NOT NULL,
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT ck_usuarios_role CHECK (role IN ('ADMIN', 'PROFISSIONAL', 'CLIENTE'))
);
