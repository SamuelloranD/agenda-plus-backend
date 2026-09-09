package com.agendaplus.scheduling.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class PeriodoAgendamento {
    private final LocalDateTime inicio;
    private final LocalDateTime fim;

    public PeriodoAgendamento(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Início e fim são obrigatórios.");
        }
        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException("O fim deve ser posterior ao início.");
        }
        this.inicio = inicio;
        this.fim = fim;
    }

    public boolean conflitaCom(PeriodoAgendamento outroPeriodo) {
        Objects.requireNonNull(outroPeriodo, "Outro período é obrigatório.");
        return inicio.isBefore(outroPeriodo.fim) && fim.isAfter(outroPeriodo.inicio);
    }

    public boolean dentroDaJanelaDeCancelamento(LocalDateTime agora) {
        if (agora == null) {
            throw new IllegalArgumentException("O momento atual é obrigatório.");
        }
        return inicio.isBefore(agora.plusHours(24));
    }

    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFim() { return fim; }
}
