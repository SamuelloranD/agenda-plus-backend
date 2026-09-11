package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.professionals.application.ProfissionalNaoEncontradoException;
import com.agendaplus.professionals.domain.model.HorarioTrabalho;
import com.agendaplus.professionals.infrastructure.persistence.ProfissionalRepository;
import com.agendaplus.scheduling.application.dto.HorarioDisponivelResponse;
import com.agendaplus.scheduling.domain.model.PeriodoAgendamento;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import com.agendaplus.scheduling.domain.service.VerificadorDeDisponibilidade;
import com.agendaplus.services.application.ServicoNaoEncontradoException;
import com.agendaplus.services.infrastructure.persistence.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ListarHorariosDisponiveisUseCase {
    private final ProfissionalRepository profissionais;
    private final ServicoRepository servicos;
    private final AgendamentoRepository agendamentos;
    private final VerificadorDeDisponibilidade verificador = new VerificadorDeDisponibilidade();

    public ListarHorariosDisponiveisUseCase(ProfissionalRepository profissionais,
                                            ServicoRepository servicos,
                                            AgendamentoRepository agendamentos) {
        this.profissionais = profissionais;
        this.servicos = servicos;
        this.agendamentos = agendamentos;
    }

    @Transactional(readOnly = true)
    public List<HorarioDisponivelResponse> executar(UUID profissionalId, LocalDate data, UUID servicoId) {
        var profissional = profissionais.findById(profissionalId).orElseThrow(ProfissionalNaoEncontradoException::new);
        var servico = servicos.findById(servicoId).orElseThrow(ServicoNaoEncontradoException::new);
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.plusDays(1).atStartOfDay();
        var existentes = agendamentos.buscarPorProfissionalEPeriodo(profissionalId, inicioDia, fimDia);
        long duracao = servico.getDuracaoMinutos();

        return profissional.getHorariosTrabalho().stream()
                .filter(horario -> horario.getDiaSemana() == data.getDayOfWeek())
                .sorted(Comparator.comparing(HorarioTrabalho::getInicio))
                .flatMap(horario -> gerarSlots(profissionalId, data, horario, duracao, existentes).stream())
                .toList();
    }

    private List<HorarioDisponivelResponse> gerarSlots(UUID profissionalId, LocalDate data,
                                                       HorarioTrabalho horario, long duracao,
                                                       List<com.agendaplus.scheduling.domain.model.Agendamento> existentes) {
        var limite = data.atTime(horario.getFim());
        var inicio = data.atTime(horario.getInicio());
        var slots = new java.util.ArrayList<HorarioDisponivelResponse>();
        while (!inicio.plusMinutes(duracao).isAfter(limite)) {
            var fim = inicio.plusMinutes(duracao);
            var periodo = new PeriodoAgendamento(inicio, fim);
            if (verificador.estaDisponivel(profissionalId, periodo, existentes)) {
                slots.add(new HorarioDisponivelResponse(inicio, fim));
            }
            inicio = fim;
        }
        return slots;
    }
}
