package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.scheduling.domain.exception.HorarioIndisponivelException;
import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.model.PeriodoAgendamento;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import com.agendaplus.scheduling.domain.service.VerificadorDeDisponibilidade;
import java.time.LocalDateTime;
import java.util.UUID;

public class CriarAgendamentoUseCase {
    private final AgendamentoRepository repository;
    private final VerificadorDeDisponibilidade disponibilidade;
    public CriarAgendamentoUseCase(AgendamentoRepository repository, VerificadorDeDisponibilidade disponibilidade) {
        this.repository = repository; this.disponibilidade = disponibilidade;
    }
    public Agendamento executar(LocalDateTime inicio, LocalDateTime fim, UUID profissionalId, UUID clienteId, UUID servicoId) {
        var periodo = new PeriodoAgendamento(inicio, fim);
        if (!disponibilidade.estaDisponivel(profissionalId, periodo,
                repository.buscarPorProfissionalEPeriodo(profissionalId, inicio, fim))) {
            throw new HorarioIndisponivelException();
        }
        return repository.salvar(new Agendamento(UUID.randomUUID(), periodo, profissionalId, clienteId, servicoId));
    }
}
