package com.agendaplus.services.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServicoRequest(@NotBlank @Size(max = 150) String nome,
                             @Min(1) int duracaoMinutos,
                             @NotNull @Valid DinheiroRequest preco) {
}
