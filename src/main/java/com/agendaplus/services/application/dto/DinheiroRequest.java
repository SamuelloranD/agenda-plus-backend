package com.agendaplus.services.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DinheiroRequest(@NotNull BigDecimal valor, @NotBlank String moeda) {
}
