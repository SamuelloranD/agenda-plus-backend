package com.agendaplus.scheduling.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PeriodoAgendamentoTest {
    private final LocalDateTime inicio = LocalDateTime.of(2026, 9, 10, 10, 0);

    @Test
    void deveCriarPeriodoQuandoFimForDepoisDoInicio() {
        var periodo = new PeriodoAgendamento(inicio, inicio.plusHours(1));

        assertEquals(inicio, periodo.getInicio());
        assertEquals(inicio.plusHours(1), periodo.getFim());
    }

    @Test
    void deveRejeitarPeriodoSemInicioOuFim() {
        assertThrows(IllegalArgumentException.class, () -> new PeriodoAgendamento(null, inicio));
        assertThrows(IllegalArgumentException.class, () -> new PeriodoAgendamento(inicio, null));
    }

    @Test
    void deveRejeitarFimIgualOuAntesDoInicio() {
        assertThrows(IllegalArgumentException.class, () -> new PeriodoAgendamento(inicio, inicio));
        assertThrows(IllegalArgumentException.class, () -> new PeriodoAgendamento(inicio, inicio.minusMinutes(1)));
    }

    @Test
    void deveIdentificarConflitoEntrePeriodosSobrepostos() {
        var periodo = new PeriodoAgendamento(inicio, inicio.plusHours(1));

        assertTrue(periodo.conflitaCom(new PeriodoAgendamento(inicio.plusMinutes(30), inicio.plusHours(2))));
    }

    @Test
    void naoDeveIdentificarConflitoEntrePeriodosApenasAdjacentes() {
        var periodo = new PeriodoAgendamento(inicio, inicio.plusHours(1));

        assertFalse(periodo.conflitaCom(new PeriodoAgendamento(inicio.plusHours(1), inicio.plusHours(2))));
    }

    @Test
    void deveConsiderarDentroDaJanelaQuandoFaltamMenosDe24Horas() {
        var periodo = new PeriodoAgendamento(inicio, inicio.plusHours(1));

        assertTrue(periodo.dentroDaJanelaDeCancelamento(inicio.minusHours(23)));
        assertFalse(periodo.dentroDaJanelaDeCancelamento(inicio.minusHours(24)));
    }
}
