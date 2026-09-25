package com.agendaplus.professionals.application.dto;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class HorarioTrabalhoRequestTest {
    @Test
    void aceitaFimPosteriorAoInicio() {
        new HorarioTrabalhoRequest(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0))
                .validarPeriodo();
    }

    @Test
    void rejeitaFimQueNaoSejaPosteriorAoInicio() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                new HorarioTrabalhoRequest(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 0))
                        .validarPeriodo());
    }
}
