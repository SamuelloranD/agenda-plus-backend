package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.scheduling.domain.exception.HorarioIndisponivelException;
import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.model.PeriodoAgendamento;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import com.agendaplus.scheduling.domain.service.VerificadorDeDisponibilidade;
import com.agendaplus.professionals.application.ProfissionalNaoEncontradoException;
import com.agendaplus.professionals.infrastructure.persistence.ProfissionalRepository;
import java.time.LocalDateTime;
import java.util.UUID;

public class CriarAgendamentoUseCase {
    private final AgendamentoRepository repository;
    private final VerificadorDeDisponibilidade disponibilidade;
    private final ProfissionalRepository profissionais;
    public CriarAgendamentoUseCase(AgendamentoRepository repository, VerificadorDeDisponibilidade disponibilidade,
                                   ProfissionalRepository profissionais) {
        this.repository = repository; this.disponibilidade = disponibilidade; this.profissionais = profissionais;
    }
    public Agendamento executar(LocalDateTime inicio, LocalDateTime fim, UUID profissionalId, UUID clienteId, UUID servicoId) {
        var periodo = new PeriodoAgendamento(inicio, fim);
        var profissional = profissionais.findById(profissionalId).orElseThrow(ProfissionalNaoEncontradoException::new);
        if (!profissional.isAtivo()) throw new HorarioIndisponivelException();
        if (!disponibilidade.estaDisponivel(profissionalId, periodo,
                repository.buscarPorProfissionalEPeriodo(profissionalId, inicio, fim))) {
            throw new HorarioIndisponivelException();
        }
        return repository.salvar(new Agendamento(UUID.randomUUID(), periodo, profissionalId, clienteId, servicoId));
    }
}
