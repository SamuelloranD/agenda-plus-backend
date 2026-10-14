package com.agendaplus.professionals.application.dto;

import org.junit.jupiter.api.Test;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ProfissionalRequestTest {
    private HorarioTrabalhoRequest faixa(int inicio, int fim) {
        return new HorarioTrabalhoRequest(DayOfWeek.MONDAY, LocalTime.of(inicio, 0), LocalTime.of(fim, 0));
    }

    @Test
    void aceitaPeriodosAdjacentesENenhumHorario() {
        new ProfissionalRequest("Ana", "Barbeira", null, List.of(faixa(9, 12), faixa(12, 18)), true).validarHorarios();
        new ProfissionalRequest("Ana", "Barbeira", null, List.of(), true).validarHorarios();
    }

    @Test
    void rejeitaPeriodosSobrepostos() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                new ProfissionalRequest("Ana", "Barbeira", null,
                        List.of(faixa(9, 12), faixa(11, 18)), true).validarHorarios());
    }
}
