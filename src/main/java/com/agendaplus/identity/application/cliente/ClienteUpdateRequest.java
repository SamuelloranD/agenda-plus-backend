package com.agendaplus.identity.application.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteUpdateRequest(@NotBlank @Size(max = 150) String nome,
                                   @NotBlank @Email String email) {
}
