package com.agendaplus.identity.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroRequest(@NotBlank @Size(max = 150) String nome,
                              @NotBlank String email, @NotBlank String senha) {
    @Override
    public String toString() { return "CadastroRequest[credenciais=PROTEGIDAS]"; }
}
