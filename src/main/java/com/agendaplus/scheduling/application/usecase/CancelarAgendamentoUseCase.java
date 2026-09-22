package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.scheduling.domain.exception.AgendamentoNaoEncontradoException;
import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import com.agendaplus.scheduling.domain.event.AgendamentoCancelado;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

public class CancelarAgendamentoUseCase {
    private final AgendamentoRepository repository; private final Clock clock; private final ApplicationEventPublisher eventPublisher;
    public CancelarAgendamentoUseCase(AgendamentoRepository repository, Clock clock, ApplicationEventPublisher eventPublisher) {
        this.repository = repository; this.clock = clock; this.eventPublisher = eventPublisher;
    }
    @Transactional
    public Agendamento executar(UUID id, UUID solicitanteId, boolean administrador) {
        Agendamento a = repository.buscarPorId(id).orElseThrow(AgendamentoNaoEncontradoException::new);
        if (!administrador && !a.getClienteId().equals(solicitanteId)) {
            throw new AccessDeniedException("O cliente autenticado não pode cancelar este agendamento.");
        }
        if (administrador) {
            a.cancelarSemJanelaDeAntecedencia();
        } else {
            a.cancelar(LocalDateTime.now(clock));
        }
        Agendamento salvo = repository.salvar(a);
        eventPublisher.publishEvent(new AgendamentoCancelado(salvo.getId(), salvo.getProfissionalId(), salvo.getClienteId(),
                salvo.getServicoId(), salvo.getPeriodo()));
        return salvo;
    }
}
