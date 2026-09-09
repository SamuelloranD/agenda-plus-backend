package com.agendaplus.scheduling.infrastructure;

import com.agendaplus.scheduling.application.usecase.*;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import com.agendaplus.scheduling.domain.service.VerificadorDeDisponibilidade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;

@Configuration
public class SchedulingConfiguration {
    @Bean Clock clock() { return Clock.systemUTC(); }
    @Bean VerificadorDeDisponibilidade verificadorDeDisponibilidade() { return new VerificadorDeDisponibilidade(); }
    @Bean CriarAgendamentoUseCase criarAgendamentoUseCase(AgendamentoRepository r, VerificadorDeDisponibilidade v) { return new CriarAgendamentoUseCase(r, v); }
    @Bean CancelarAgendamentoUseCase cancelarAgendamentoUseCase(AgendamentoRepository r, Clock c) { return new CancelarAgendamentoUseCase(r, c); }
    @Bean ConfirmarAgendamentoUseCase confirmarAgendamentoUseCase(AgendamentoRepository r) { return new ConfirmarAgendamentoUseCase(r); }
    @Bean ListarAgendamentosUseCase listarAgendamentosUseCase(AgendamentoRepository r) { return new ListarAgendamentosUseCase(r); }
}
