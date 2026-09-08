package com.agendaplus.identity.application.dto;

import com.agendaplus.identity.domain.model.Role;
import com.agendaplus.identity.domain.model.Usuario;
import java.util.UUID;

public record UsuarioResponse(UUID id, String nome, String email, Role role) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail().valor(), usuario.getRole());
    }
}
