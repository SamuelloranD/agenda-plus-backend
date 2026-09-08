package com.agendaplus.identity.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String email, @NotBlank String senha) {
    @Override
    public String toString() { return "LoginRequest[credenciais=PROTEGIDAS]"; }
}
