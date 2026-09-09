package com.agendaplus.scheduling.domain.event;

import com.agendaplus.scheduling.domain.model.PeriodoAgendamento;

import java.util.UUID;

public record AgendamentoConfirmado(UUID agendamentoId, UUID profissionalId, UUID clienteId,
                                    UUID servicoId, PeriodoAgendamento periodo) {
}
