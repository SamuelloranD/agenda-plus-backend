package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.scheduling.domain.exception.CancelamentoNaoPermitidoException;
import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.model.PeriodoAgendamento;
import com.agendaplus.scheduling.domain.model.StatusAgendamento;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import org.springframework.context.ApplicationEventPublisher;
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
            "2026-09-19T13:59:59.999Z, true",
            "2026-09-19T14:00:00.000Z, true",
            "2026-09-19T14:00:00.001Z, false"
    })
    void aplicaLimiteDe24HorasParaCliente(String agora, boolean permitido) {
        var repository = mock(AgendamentoRepository.class);
        var publisher = mock(ApplicationEventPublisher.class);
        var inicio = LocalDateTime.of(2026, 9, 20, 14, 0);
        var agendamento = new Agendamento(UUID.randomUUID(), new PeriodoAgendamento(inicio, inicio.plusHours(1)),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(repository.buscarPorId(agendamento.getId())).thenReturn(Optional.of(agendamento));
        var useCase = new CancelarAgendamentoUseCase(repository, Clock.fixed(Instant.parse(agora), ZoneOffset.UTC), publisher);
        var solicitante = agendamento.getClienteId();

        if (permitido) {
            when(repository.salvar(agendamento)).thenReturn(agendamento);
            assertEquals(StatusAgendamento.CANCELADO, useCase.executar(agendamento.getId(), solicitante, false).getStatus());
            verify(repository).salvar(agendamento);
            verify(publisher).publishEvent(any(Object.class));
        } else {
            assertThrows(CancelamentoNaoPermitidoException.class,
                    () -> useCase.executar(agendamento.getId(), solicitante, false));
            assertEquals(StatusAgendamento.PENDENTE, agendamento.getStatus());
            verify(repository, never()).salvar(any());
            verifyNoInteractions(publisher);
        }
    }

    @org.junit.jupiter.api.Test
    void administradorCancelaDentroDaJanelaEPublicaMesmoEvento() {
        var repository = mock(AgendamentoRepository.class);
        var publisher = mock(ApplicationEventPublisher.class);
        var agendamento = new Agendamento(UUID.randomUUID(),
                new PeriodoAgendamento(LocalDateTime.of(2026, 9, 20, 14, 0), LocalDateTime.of(2026, 9, 20, 15, 0)),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(repository.buscarPorId(agendamento.getId())).thenReturn(Optional.of(agendamento));
        when(repository.salvar(agendamento)).thenReturn(agendamento);
        var useCase = new CancelarAgendamentoUseCase(repository,
                Clock.fixed(Instant.parse("2026-09-20T10:00:00Z"), ZoneOffset.UTC), publisher);

        var resultado = useCase.executar(agendamento.getId(), UUID.randomUUID(), true);

        assertEquals(StatusAgendamento.CANCELADO, resultado.getStatus());
        verify(repository).salvar(agendamento);
        verify(publisher).publishEvent(any(com.agendaplus.scheduling.domain.event.AgendamentoCancelado.class));
    }

    @org.junit.jupiter.api.Test
    void naoPublicaEventoQuandoSolicitanteNaoEhDono() {
        var repository = mock(AgendamentoRepository.class);
        var publisher = mock(ApplicationEventPublisher.class);
        var agendamento = new Agendamento(UUID.randomUUID(),
                new PeriodoAgendamento(LocalDateTime.of(2026, 9, 25, 14, 0), LocalDateTime.of(2026, 9, 25, 15, 0)),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(repository.buscarPorId(agendamento.getId())).thenReturn(Optional.of(agendamento));
        var useCase = new CancelarAgendamentoUseCase(repository,
                Clock.fixed(Instant.parse("2026-09-21T10:00:00Z"), ZoneOffset.UTC), publisher);

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> useCase.executar(agendamento.getId(), UUID.randomUUID(), false));
        verifyNoInteractions(publisher);
    }

    @org.junit.jupiter.api.Test
    void naoPublicaEventoQuandoTransicaoEhInvalida() {
        var repository = mock(AgendamentoRepository.class);
        var publisher = mock(ApplicationEventPublisher.class);
        var agendamento = new Agendamento(UUID.randomUUID(),
                new PeriodoAgendamento(LocalDateTime.of(2026, 9, 25, 14, 0), LocalDateTime.of(2026, 9, 25, 15, 0)),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        agendamento.cancelar(LocalDateTime.of(2026, 9, 18, 10, 0));
        when(repository.buscarPorId(agendamento.getId())).thenReturn(Optional.of(agendamento));
        var useCase = new CancelarAgendamentoUseCase(repository,
                Clock.fixed(Instant.parse("2026-09-21T10:00:00Z"), ZoneOffset.UTC), publisher);

        assertThrows(com.agendaplus.scheduling.domain.exception.TransicaoInvalidaException.class,
                () -> useCase.executar(agendamento.getId(), agendamento.getClienteId(), false));
        verifyNoInteractions(publisher);
    }
}
