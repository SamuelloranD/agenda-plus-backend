package com.agendaplus.scheduling.application.dto;

import java.time.LocalDateTime;

public record HorarioDisponivelResponse(LocalDateTime inicio, LocalDateTime fim) {
}
