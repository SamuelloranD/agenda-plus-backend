CREATE TABLE agendamentos (
    id UUID PRIMARY KEY,
    periodo_inicio TIMESTAMP NOT NULL,
    periodo_fim TIMESTAMP NOT NULL,
    profissional_id UUID NOT NULL,
    cliente_id UUID NOT NULL,
    servico_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT ck_agendamentos_periodo CHECK (periodo_fim > periodo_inicio),
    CONSTRAINT ck_agendamentos_status CHECK (status IN ('PENDENTE', 'CONFIRMADO', 'CANCELADO', 'CONCLUIDO'))
);

CREATE INDEX ix_agendamentos_profissional_periodo
    ON agendamentos (profissional_id, periodo_inicio, periodo_fim);
CREATE INDEX ix_agendamentos_periodo_inicio ON agendamentos (periodo_inicio);
