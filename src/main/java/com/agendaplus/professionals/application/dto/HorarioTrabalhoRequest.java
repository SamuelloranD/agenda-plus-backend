package com.agendaplus.professionals.application.dto;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record HorarioTrabalhoRequest(@NotNull DayOfWeek diaSemana,
                                     @NotNull LocalTime inicio,
                                     @NotNull LocalTime fim) {
    public void validarPeriodo() {
        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException("O fim deve ser posterior ao início.");
        }
    }
}
