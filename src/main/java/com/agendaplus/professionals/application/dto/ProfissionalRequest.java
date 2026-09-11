package com.agendaplus.professionals.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProfissionalRequest(@NotBlank @Size(max = 150) String nome,
                                  @NotBlank @Size(max = 150) String especialidade,
                                  @NotNull @Valid List<HorarioTrabalhoRequest> horariosTrabalho) {
    public void validarHorarios() { horariosTrabalho.forEach(HorarioTrabalhoRequest::validarPeriodo); }
}
