package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.scheduling.domain.exception.AgendamentoNaoEncontradoException;
import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import com.agendaplus.scheduling.application.port.NotificadorDeAgendamento;
import com.agendaplus.scheduling.domain.event.AgendamentoConfirmado;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

public class ConfirmarAgendamentoUseCase {
    private final AgendamentoRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    public ConfirmarAgendamentoUseCase(AgendamentoRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository; this.eventPublisher = eventPublisher;
    }
    @Transactional
    public Agendamento executar(UUID id) {
        Agendamento a = repository.buscarPorId(id).orElseThrow(AgendamentoNaoEncontradoException::new);
        a.confirmar();
        Agendamento salvo = repository.salvar(a);
        eventPublisher.publishEvent(new AgendamentoConfirmado(salvo.getId(), salvo.getProfissionalId(), salvo.getClienteId(),
                salvo.getServicoId(), salvo.getPeriodo()));
        return salvo;
    }
}
