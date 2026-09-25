package com.agendaplus.professionals.domain.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class HorarioTrabalhoTest {
    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final LocalTime INICIO = LocalTime.of(9, 0);
    private static final LocalTime FIM = LocalTime.of(12, 0);

    @Test
    void criaHorarioValidoComTodosOsDados() {
        var horario = new HorarioTrabalho(ID, DayOfWeek.MONDAY, INICIO, FIM);

        assertThat(horario.getId()).isEqualTo(ID);
        assertThat(horario.getDiaSemana()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(horario.getInicio()).isEqualTo(INICIO);
        assertThat(horario.getFim()).isEqualTo(FIM);
    }

    @Test
    void rejeitaDadosObrigatoriosAusentes() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HorarioTrabalho(null, DayOfWeek.MONDAY, INICIO, FIM));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HorarioTrabalho(ID, null, INICIO, FIM));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HorarioTrabalho(ID, DayOfWeek.MONDAY, null, FIM));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HorarioTrabalho(ID, DayOfWeek.MONDAY, INICIO, null));
    }

    @Test
    void rejeitaIntervaloSemDuracaoPositiva() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HorarioTrabalho(ID, DayOfWeek.MONDAY, INICIO, INICIO));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HorarioTrabalho(ID, DayOfWeek.MONDAY, INICIO, INICIO.minusMinutes(1)));
    }

    @Test
    void rejeitaNovoFimInvalidoAoAtualizarFaixa() {
        var horario = new HorarioTrabalho(ID, DayOfWeek.MONDAY, INICIO, FIM);

        assertThatIllegalArgumentException().isThrownBy(() -> horario.atualizarFim(null));
        assertThatIllegalArgumentException().isThrownBy(() -> horario.atualizarFim(INICIO));
    }
}
