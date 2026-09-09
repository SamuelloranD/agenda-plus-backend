package com.agendaplus.scheduling.domain.service;

import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.model.PeriodoAgendamento;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VerificadorDeDisponibilidadeTest {
    private final UUID profissionalId = UUID.randomUUID();
    private final LocalDateTime inicio = LocalDateTime.of(2026, 9, 10, 10, 0);

    private Agendamento agendamento(UUID profissional, PeriodoAgendamento periodo) {
        return new Agendamento(UUID.randomUUID(), periodo, profissional, UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    void deveRetornarIndisponivelQuandoHouverConflitoComMesmoProfissional() {
        var existentes = List.of(agendamento(profissionalId,
                new PeriodoAgendamento(inicio, inicio.plusHours(1))));
        var novoPeriodo = new PeriodoAgendamento(inicio.plusMinutes(30), inicio.plusHours(2));

        assertFalse(new VerificadorDeDisponibilidade().estaDisponivel(profissionalId, novoPeriodo, existentes));
    }

    @Test
    void deveRetornarDisponivelQuandoNaoHouverConflito() {
        var existentes = List.of(agendamento(profissionalId,
                new PeriodoAgendamento(inicio, inicio.plusHours(1))));
        var novoPeriodo = new PeriodoAgendamento(inicio.plusHours(1), inicio.plusHours(2));

        assertTrue(new VerificadorDeDisponibilidade().estaDisponivel(profissionalId, novoPeriodo, existentes));
    }

    @Test
    void deveIgnorarAgendamentoCancelado() {
        var cancelado = agendamento(profissionalId, new PeriodoAgendamento(inicio, inicio.plusHours(1)));
        cancelado.cancelar(inicio.minusHours(24));

        assertTrue(new VerificadorDeDisponibilidade().estaDisponivel(profissionalId,
                new PeriodoAgendamento(inicio.plusMinutes(30), inicio.plusHours(2)), List.of(cancelado)));
    }

    @Test
    void deveIgnorarAgendamentoDeOutroProfissional() {
        var existente = agendamento(UUID.randomUUID(), new PeriodoAgendamento(inicio, inicio.plusHours(1)));

        assertTrue(new VerificadorDeDisponibilidade().estaDisponivel(profissionalId,
                new PeriodoAgendamento(inicio.plusMinutes(30), inicio.plusHours(2)), List.of(existente)));
    }
}
