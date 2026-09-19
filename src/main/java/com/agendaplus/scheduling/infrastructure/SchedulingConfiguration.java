package com.agendaplus.scheduling.infrastructure;

import com.agendaplus.scheduling.application.usecase.*;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import com.agendaplus.scheduling.domain.service.VerificadorDeDisponibilidade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;
import org.springframework.context.ApplicationEventPublisher;

@Configuration
public class SchedulingConfiguration {
    @Bean Clock clock() { return Clock.systemUTC(); }
    @Bean VerificadorDeDisponibilidade verificadorDeDisponibilidade() { return new VerificadorDeDisponibilidade(); }
    @Bean CriarAgendamentoUseCase criarAgendamentoUseCase(AgendamentoRepository r, VerificadorDeDisponibilidade v) { return new CriarAgendamentoUseCase(r, v); }
    @Bean CancelarAgendamentoUseCase cancelarAgendamentoUseCase(AgendamentoRepository r, Clock c, ApplicationEventPublisher p) { return new CancelarAgendamentoUseCase(r, c, p); }
    @Bean ConfirmarAgendamentoUseCase confirmarAgendamentoUseCase(AgendamentoRepository r, ApplicationEventPublisher p) { return new ConfirmarAgendamentoUseCase(r, p); }
    @Bean ListarAgendamentosUseCase listarAgendamentosUseCase(AgendamentoRepository r) { return new ListarAgendamentosUseCase(r); }
}
