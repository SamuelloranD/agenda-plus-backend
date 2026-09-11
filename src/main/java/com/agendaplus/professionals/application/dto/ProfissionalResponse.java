package com.agendaplus.professionals.application.dto;

import com.agendaplus.professionals.domain.model.Profissional;

import java.util.List;
import java.util.UUID;

public record ProfissionalResponse(UUID id, String nome, String especialidade,
                                   List<HorarioTrabalhoResponse> horariosTrabalho) {
    public static ProfissionalResponse from(Profissional profissional) {
        return new ProfissionalResponse(profissional.getId(), profissional.getNome(), profissional.getEspecialidade(),
                profissional.getHorariosTrabalho().stream().map(HorarioTrabalhoResponse::from).toList());
    }
}
