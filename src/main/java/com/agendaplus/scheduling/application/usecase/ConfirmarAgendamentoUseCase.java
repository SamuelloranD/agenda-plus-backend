package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.scheduling.domain.exception.AgendamentoNaoEncontradoException;
import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import java.util.UUID;

public class ConfirmarAgendamentoUseCase {
    private final AgendamentoRepository repository;
    public ConfirmarAgendamentoUseCase(AgendamentoRepository repository) { this.repository = repository; }
    public Agendamento executar(UUID id) {
        Agendamento a = repository.buscarPorId(id).orElseThrow(AgendamentoNaoEncontradoException::new);
        a.confirmar(); return repository.salvar(a);
    }
}
