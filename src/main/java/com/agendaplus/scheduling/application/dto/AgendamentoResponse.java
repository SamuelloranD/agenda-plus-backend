package com.agendaplus.scheduling.application.dto;

import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.model.StatusAgendamento;
import java.time.LocalDateTime;
import java.util.UUID;

public record AgendamentoResponse(UUID id, LocalDateTime inicio, LocalDateTime fim, UUID profissionalId,
                                  UUID clienteId, UUID servicoId, StatusAgendamento status) {
    public static AgendamentoResponse from(Agendamento a) {
        return new AgendamentoResponse(a.getId(), a.getPeriodo().getInicio(), a.getPeriodo().getFim(),
                a.getProfissionalId(), a.getClienteId(), a.getServicoId(), a.getStatus());
    }
}
