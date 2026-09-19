package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.scheduling.domain.exception.TransicaoInvalidaException;
import com.agendaplus.scheduling.domain.exception.AgendamentoNaoEncontradoException;
import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.model.PeriodoAgendamento;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConfirmarAgendamentoUseCaseTest {
    @Test
    void publicaEventoDepoisDeConfirmarEGuardar() {
        var repository = mock(AgendamentoRepository.class);
        var publisher = mock(ApplicationEventPublisher.class);
        var agendamento = agendamento();
        when(repository.buscarPorId(agendamento.getId())).thenReturn(Optional.of(agendamento));
        when(repository.salvar(agendamento)).thenReturn(agendamento);

        new ConfirmarAgendamentoUseCase(repository, publisher).executar(agendamento.getId());

        verify(repository).salvar(agendamento);
        verify(publisher).publishEvent(any(Object.class));
    }

    @Test
    void naoPublicaEventoQuandoTransicaoFalha() {
        var repository = mock(AgendamentoRepository.class);
        var publisher = mock(ApplicationEventPublisher.class);
        var agendamento = agendamento();
        agendamento.confirmar();
        when(repository.buscarPorId(agendamento.getId())).thenReturn(Optional.of(agendamento));

        assertThrows(TransicaoInvalidaException.class,
                () -> new ConfirmarAgendamentoUseCase(repository, publisher).executar(agendamento.getId()));
        verify(repository, never()).salvar(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void rejeitaAgendamentoInexistenteSemEfeitosColaterais() {
        var repository = mock(AgendamentoRepository.class);
        var publisher = mock(ApplicationEventPublisher.class);
        var id = UUID.randomUUID();
        when(repository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(AgendamentoNaoEncontradoException.class,
                () -> new ConfirmarAgendamentoUseCase(repository, publisher).executar(id));

        verify(repository, never()).salvar(any());
        verifyNoInteractions(publisher);
    }

    private Agendamento agendamento() {
        var inicio = LocalDateTime.of(2026, 9, 20, 14, 0);
        return new Agendamento(UUID.randomUUID(), new PeriodoAgendamento(inicio, inicio.plusHours(1)),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }
}
