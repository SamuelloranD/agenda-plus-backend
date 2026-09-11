package com.agendaplus.identity.domain.repository;

import com.agendaplus.identity.domain.model.Email;
import com.agendaplus.identity.domain.model.Role;
import com.agendaplus.identity.domain.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    Optional<Usuario> buscarPorEmail(Email email);
    Optional<Usuario> buscarPorId(UUID id);
    List<Usuario> listarPorRole(Role role);
    Usuario salvar(Usuario usuario);
    void excluir(UUID id);
}
