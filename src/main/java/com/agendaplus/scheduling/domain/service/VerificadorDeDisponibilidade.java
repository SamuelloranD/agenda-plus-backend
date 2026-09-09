package com.agendaplus.scheduling.domain.service;

import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.model.PeriodoAgendamento;
import com.agendaplus.scheduling.domain.model.StatusAgendamento;

import java.util.List;
import java.util.UUID;

public final class VerificadorDeDisponibilidade {
    public boolean estaDisponivel(UUID profissionalId, PeriodoAgendamento novoPeriodo,
                                  List<Agendamento> agendamentosExistentes) {
        if (profissionalId == null || novoPeriodo == null || agendamentosExistentes == null) {
            throw new IllegalArgumentException("Profissional, período e agendamentos são obrigatórios.");
        }
        return agendamentosExistentes.stream()
                .filter(agendamento -> agendamento.getProfissionalId().equals(profissionalId))
                .filter(agendamento -> agendamento.getStatus() != StatusAgendamento.CANCELADO)
                .noneMatch(agendamento -> agendamento.getPeriodo().conflitaCom(novoPeriodo));
    }
}
