package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.professionals.domain.model.Profissional;
import com.agendaplus.professionals.domain.model.HorarioTrabalho;
import com.agendaplus.professionals.infrastructure.persistence.ProfissionalRepository;
import com.agendaplus.services.infrastructure.persistence.ServicoRepository;
import com.agendaplus.services.domain.model.Servico;
import com.agendaplus.shared.domain.model.Dinheiro;
import java.math.BigDecimal;
import java.util.Currency;
import com.agendaplus.scheduling.domain.exception.HorarioIndisponivelException;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import com.agendaplus.scheduling.domain.service.VerificadorDeDisponibilidade;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CriarAgendamentoUseCaseTest {
    @Test
    void rejeitaHorarioForaDaJornadaEDuracaoDiferenteDoServico() {
        var profissionalId = UUID.randomUUID();
        var servicoId = UUID.randomUUID();
        var profissional = new Profissional(profissionalId, "Ana", "Barbeira");
        profissional.substituirHorarios(List.of(new HorarioTrabalho(UUID.randomUUID(), DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))));
        var profissionais = mock(ProfissionalRepository.class);
        var servicos = mock(ServicoRepository.class);
        var agendamentos = mock(AgendamentoRepository.class);
        when(profissionais.findForScheduling(profissionalId)).thenReturn(Optional.of(profissional));
        when(servicos.findById(servicoId)).thenReturn(Optional.of(new Servico(servicoId, "Corte", 30, new Dinheiro(BigDecimal.TEN, Currency.getInstance("BRL")))));
        var useCase = new CriarAgendamentoUseCase(agendamentos, new VerificadorDeDisponibilidade(), profissionais, servicos);
        var clienteId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.executar(LocalDateTime.of(2027, 1, 4, 8, 30), LocalDateTime.of(2027, 1, 4, 9, 0), profissionalId, clienteId, servicoId))
                .isInstanceOf(HorarioIndisponivelException.class);
        assertThatThrownBy(() -> useCase.executar(LocalDateTime.of(2027, 1, 4, 9, 0), LocalDateTime.of(2027, 1, 4, 9, 45), profissionalId, clienteId, servicoId))
                .isInstanceOf(HorarioIndisponivelException.class);
        verifyNoInteractions(agendamentos);
    }
    @Test
    void servicoInativoNaoPodeSerAgendadoDiretamente() {
        var profissionalId = UUID.randomUUID();
        var servicoId = UUID.randomUUID();
        var profissionais = mock(ProfissionalRepository.class);
        var servicos = mock(ServicoRepository.class);
        var agendamentos = mock(AgendamentoRepository.class);
        when(profissionais.findForScheduling(profissionalId)).thenReturn(Optional.of(new Profissional(profissionalId, "Ana", "Barbeira")));
        var servico = new Servico(servicoId, "Corte", 30, new Dinheiro(BigDecimal.TEN, Currency.getInstance("BRL")));
        servico.definirAtivo(false);
        when(servicos.findById(servicoId)).thenReturn(Optional.of(servico));
        var inicio = LocalDateTime.of(2027, 1, 4, 9, 0);

        assertThatThrownBy(() -> new CriarAgendamentoUseCase(agendamentos,
                new VerificadorDeDisponibilidade(), profissionais, servicos)
                .executar(inicio, inicio.plusMinutes(30), profissionalId, UUID.randomUUID(), servicoId))
                .isInstanceOf(HorarioIndisponivelException.class);
        verifyNoInteractions(agendamentos);
    }
    @Test
    void profissionalInativoNaoRecebeNovoAgendamento() {
        var id = UUID.randomUUID();
        var profissional = new Profissional(id, "Ana", "Barbeira");
        profissional.definirAtivo(false);
        var profissionais = mock(ProfissionalRepository.class);
        var agendamentos = mock(AgendamentoRepository.class);
        when(profissionais.findForScheduling(id)).thenReturn(Optional.of(profissional));
        var inicio = LocalDateTime.of(2027, 1, 4, 9, 0);

        assertThatThrownBy(() -> new CriarAgendamentoUseCase(agendamentos,
                new VerificadorDeDisponibilidade(), profissionais, mock(ServicoRepository.class))
                .executar(inicio, inicio.plusMinutes(30), id, UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(HorarioIndisponivelException.class);
        verifyNoInteractions(agendamentos);
    }
}
