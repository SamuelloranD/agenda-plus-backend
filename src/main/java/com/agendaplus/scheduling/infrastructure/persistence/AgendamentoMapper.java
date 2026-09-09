package com.agendaplus.scheduling.infrastructure.persistence;

import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.model.PeriodoAgendamento;
import org.springframework.stereotype.Component;

@Component
public class AgendamentoMapper {
    public AgendamentoJpaEntity toEntity(Agendamento agendamento) {
        return new AgendamentoJpaEntity(agendamento.getId(), agendamento.getPeriodo().getInicio(),
                agendamento.getPeriodo().getFim(), agendamento.getProfissionalId(), agendamento.getClienteId(),
                agendamento.getServicoId(), agendamento.getStatus());
    }

    public Agendamento toDomain(AgendamentoJpaEntity entity) {
        return Agendamento.reidratar(entity.getId(),
                new PeriodoAgendamento(entity.getPeriodoInicio(), entity.getPeriodoFim()),
                entity.getProfissionalId(), entity.getClienteId(), entity.getServicoId(), entity.getStatus());
    }
}
