package com.agendaplus.scheduling.application.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record CriarAgendamentoRequest(
        @NotNull @Future LocalDateTime inicio,
        @NotNull @Future LocalDateTime fim,
        @NotNull UUID profissionalId,
        @NotNull UUID clienteId,
        @NotNull UUID servicoId) {}
