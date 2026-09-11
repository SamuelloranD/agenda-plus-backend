package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.professionals.domain.model.HorarioTrabalho;
import com.agendaplus.professionals.domain.model.Profissional;
import com.agendaplus.professionals.infrastructure.persistence.ProfissionalRepository;
import com.agendaplus.services.domain.model.Servico;
import com.agendaplus.services.infrastructure.persistence.ServicoRepository;
import com.agendaplus.shared.domain.model.Dinheiro;
import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.model.PeriodoAgendamento;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ListarHorariosDisponiveisUseCaseTest {
    private final ProfissionalRepository profissionais = mock(ProfissionalRepository.class);
    private final ServicoRepository servicos = mock(ServicoRepository.class);
    private final AgendamentoRepository agendamentos = mock(AgendamentoRepository.class);
    private final ListarHorariosDisponiveisUseCase useCase = new ListarHorariosDisponiveisUseCase(profissionais, servicos, agendamentos);
    private final UUID profissionalId = UUID.randomUUID();
    private final UUID servicoId = UUID.randomUUID();
    private final LocalDate data = LocalDate.of(2026, 9, 14);

    @Test
    void geraSlotsNaGradeDoExpedienteEIgnoraSobraInsuficiente() {
        prepararProfissional(new HorarioTrabalho(UUID.randomUUID(), DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 30)));
        prepararServico(60);
        when(agendamentos.buscarPorProfissionalEPeriodo(any(), any(), any())).thenReturn(List.of());

        var slots = useCase.executar(profissionalId, data, servicoId);

        assertThat(slots).extracting("inicio").containsExactly(
                LocalDateTime.of(2026, 9, 14, 9, 0),
                LocalDateTime.of(2026, 9, 14, 10, 0),
                LocalDateTime.of(2026, 9, 14, 11, 0));
    }

    @Test
    void removeSlotConflitanteMasMantemSlotBloqueadoPorAgendamentoCancelado() {
        prepararProfissional(new HorarioTrabalho(UUID.randomUUID(), DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0)));
        prepararServico(60);
        var cliente = UUID.randomUUID();
        var conflito = new Agendamento(UUID.randomUUID(),
                new PeriodoAgendamento(LocalDateTime.of(2026, 9, 14, 10, 0), LocalDateTime.of(2026, 9, 14, 11, 0)),
                profissionalId, cliente, servicoId);
        var cancelado = new Agendamento(UUID.randomUUID(),
                new PeriodoAgendamento(LocalDateTime.of(2026, 9, 14, 11, 0), LocalDateTime.of(2026, 9, 14, 12, 0)),
                profissionalId, cliente, servicoId);
        cancelado.cancelar(LocalDateTime.of(2026, 9, 13, 10, 0));
        when(agendamentos.buscarPorProfissionalEPeriodo(any(), any(), any())).thenReturn(List.of(conflito, cancelado));

        var slots = useCase.executar(profissionalId, data, servicoId);

        assertThat(slots).extracting("inicio").containsExactly(
                LocalDateTime.of(2026, 9, 14, 9, 0),
                LocalDateTime.of(2026, 9, 14, 11, 0));
    }

    private void prepararProfissional(HorarioTrabalho... horarios) {
        var profissional = new Profissional(profissionalId, "Ana", "Corte");
        profissional.substituirHorarios(List.of(horarios));
        when(profissionais.findById(profissionalId)).thenReturn(Optional.of(profissional));
    }

    private void prepararServico(int duracao) {
        var servico = new Servico(servicoId, "Corte", duracao,
                new Dinheiro(new BigDecimal("50.00"), Currency.getInstance("BRL")));
        when(servicos.findById(servicoId)).thenReturn(Optional.of(servico));
    }
}
