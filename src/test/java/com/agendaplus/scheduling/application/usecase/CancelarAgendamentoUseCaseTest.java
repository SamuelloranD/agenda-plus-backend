package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.scheduling.domain.exception.CancelamentoNaoPermitidoException;
import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.model.PeriodoAgendamento;
import com.agendaplus.scheduling.domain.model.StatusAgendamento;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CancelarAgendamentoUseCaseTest {
    @ParameterizedTest
    @CsvSource({
            "false, 2026-09-19T13:59:59.999Z, true",
            "false, 2026-09-19T14:00:00.000Z, true",
            "false, 2026-09-19T14:00:00.001Z, false",
            "true, 2026-09-19T13:59:59.999Z, true",
            "true, 2026-09-19T14:00:00.000Z, true",
            "true, 2026-09-19T14:00:00.001Z, false"
    })
    void aplicaLimiteDe24HorasComRelogioUtcParaClienteEAdmin(boolean admin, String agora, boolean permitido) {
        var repository = mock(AgendamentoRepository.class);
        var inicio = LocalDateTime.of(2026, 9, 20, 14, 0);
        var agendamento = new Agendamento(UUID.randomUUID(), new PeriodoAgendamento(inicio, inicio.plusHours(1)),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(repository.buscarPorId(agendamento.getId())).thenReturn(Optional.of(agendamento));
        var useCase = new CancelarAgendamentoUseCase(repository, Clock.fixed(Instant.parse(agora), ZoneOffset.UTC));
        var solicitante = admin ? UUID.randomUUID() : agendamento.getClienteId();

        if (permitido) {
            when(repository.salvar(agendamento)).thenReturn(agendamento);
            assertEquals(StatusAgendamento.CANCELADO, useCase.executar(agendamento.getId(), solicitante, admin).getStatus());
            verify(repository).salvar(agendamento);
        } else {
            assertThrows(CancelamentoNaoPermitidoException.class,
                    () -> useCase.executar(agendamento.getId(), solicitante, admin));
            assertEquals(StatusAgendamento.PENDENTE, agendamento.getStatus());
            verify(repository, never()).salvar(any());
        }
    }
}
