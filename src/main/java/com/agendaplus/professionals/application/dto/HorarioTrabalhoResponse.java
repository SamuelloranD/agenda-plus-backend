package com.agendaplus.professionals.application.dto;

import com.agendaplus.professionals.domain.model.HorarioTrabalho;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record HorarioTrabalhoResponse(DayOfWeek diaSemana, LocalTime inicio, LocalTime fim) {
    public static HorarioTrabalhoResponse from(HorarioTrabalho horario) {
        return new HorarioTrabalhoResponse(horario.getDiaSemana(), horario.getInicio(), horario.getFim());
    }
}
