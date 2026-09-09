package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.scheduling.domain.exception.AgendamentoNaoEncontradoException;
import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

public class CancelarAgendamentoUseCase {
    private final AgendamentoRepository repository; private final Clock clock;
    public CancelarAgendamentoUseCase(AgendamentoRepository repository, Clock clock) { this.repository = repository; this.clock = clock; }
    public Agendamento executar(UUID id) {
        Agendamento a = repository.buscarPorId(id).orElseThrow(AgendamentoNaoEncontradoException::new);
        a.cancelar(LocalDateTime.now(clock)); return repository.salvar(a);
    }
}
