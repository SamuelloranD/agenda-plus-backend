package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.professionals.domain.model.Profissional;
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
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CriarAgendamentoUseCaseTest {
    @Test
    void servicoInativoNaoPodeSerAgendadoDiretamente() {
        var profissionalId = UUID.randomUUID();
        var servicoId = UUID.randomUUID();
        var profissionais = mock(ProfissionalRepository.class);
        var servicos = mock(ServicoRepository.class);
        var agendamentos = mock(AgendamentoRepository.class);
        when(profissionais.findById(profissionalId)).thenReturn(Optional.of(new Profissional(profissionalId, "Ana", "Barbeira")));
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
        when(profissionais.findById(id)).thenReturn(Optional.of(profissional));
        var inicio = LocalDateTime.of(2027, 1, 4, 9, 0);

        assertThatThrownBy(() -> new CriarAgendamentoUseCase(agendamentos,
                new VerificadorDeDisponibilidade(), profissionais, mock(ServicoRepository.class))
                .executar(inicio, inicio.plusMinutes(30), id, UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(HorarioIndisponivelException.class);
        verifyNoInteractions(agendamentos);
    }
}
