package com.agendaplus.identity.domain.repository;

import com.agendaplus.identity.domain.model.Email;
import com.agendaplus.identity.domain.model.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    Optional<Usuario> buscarPorEmail(Email email);
    Optional<Usuario> buscarPorId(UUID id);
    Usuario salvar(Usuario usuario);
}
