CREATE TABLE profissionais (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    especialidade VARCHAR(150) NOT NULL
);

CREATE TABLE horarios_trabalho (
    id UUID PRIMARY KEY,
    profissional_id UUID NOT NULL REFERENCES profissionais (id) ON DELETE CASCADE,
    dia_semana VARCHAR(20) NOT NULL,
    inicio TIME NOT NULL,
    fim TIME NOT NULL,
    CONSTRAINT ck_horarios_trabalho_dia CHECK (dia_semana IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    CONSTRAINT ck_horarios_trabalho_periodo CHECK (fim > inicio),
    CONSTRAINT uk_horarios_trabalho_faixa UNIQUE (profissional_id, dia_semana, inicio)
);

CREATE INDEX ix_horarios_trabalho_profissional_dia
    ON horarios_trabalho (profissional_id, dia_semana, inicio);

CREATE TABLE servicos (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    duracao_minutos INTEGER NOT NULL,
    preco_valor NUMERIC(19, 2) NOT NULL,
    preco_moeda VARCHAR(3) NOT NULL,
    CONSTRAINT ck_servicos_duracao CHECK (duracao_minutos > 0),
    CONSTRAINT ck_servicos_preco CHECK (preco_valor >= 0),
    CONSTRAINT ck_servicos_moeda CHECK (preco_moeda ~ '^[A-Z]{3}$')
);
