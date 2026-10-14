package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.scheduling.domain.exception.HorarioIndisponivelException;
import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.model.PeriodoAgendamento;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import com.agendaplus.scheduling.domain.service.VerificadorDeDisponibilidade;
import com.agendaplus.professionals.application.ProfissionalNaoEncontradoException;
import com.agendaplus.professionals.infrastructure.persistence.ProfissionalRepository;
import com.agendaplus.services.infrastructure.persistence.ServicoRepository;
import com.agendaplus.services.application.ServicoNaoEncontradoException;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class CriarAgendamentoUseCase {
    private final AgendamentoRepository repository;
    private final VerificadorDeDisponibilidade disponibilidade;
    private final ProfissionalRepository profissionais;
    private final ServicoRepository servicos;
    public CriarAgendamentoUseCase(AgendamentoRepository repository, VerificadorDeDisponibilidade disponibilidade,
                                   ProfissionalRepository profissionais, ServicoRepository servicos) {
        this.repository = repository; this.disponibilidade = disponibilidade; this.profissionais = profissionais;
        this.servicos = servicos;
    }
    @Transactional
    public Agendamento executar(LocalDateTime inicio, LocalDateTime fim, UUID profissionalId, UUID clienteId, UUID servicoId) {
        var periodo = new PeriodoAgendamento(inicio, fim);
        var profissional = profissionais.findForScheduling(profissionalId).orElseThrow(ProfissionalNaoEncontradoException::new);
        if (!profissional.isAtivo()) throw new HorarioIndisponivelException();
        var servico = servicos.findById(servicoId).orElseThrow(ServicoNaoEncontradoException::new);
        if (!servico.isAtivo()) throw new HorarioIndisponivelException();
        if (Duration.between(inicio, fim).toMinutes() != servico.getDuracaoMinutos()
                || (!profissional.getHorariosTrabalho().isEmpty()
                && !profissional.getHorariosTrabalho().stream().anyMatch(horario ->
                        horario.getDiaSemana() == inicio.getDayOfWeek()
                        && !inicio.toLocalTime().isBefore(horario.getInicio())
                        && !fim.toLocalTime().isAfter(horario.getFim())
                        && inicio.toLocalDate().equals(fim.toLocalDate())))) {
            throw new HorarioIndisponivelException();
        }
        if (!disponibilidade.estaDisponivel(profissionalId, periodo,
                repository.buscarPorProfissionalEPeriodo(profissionalId, inicio, fim))) {
            throw new HorarioIndisponivelException();
        }
        return repository.salvar(new Agendamento(UUID.randomUUID(), periodo, profissionalId, clienteId, servicoId));
    }
}
