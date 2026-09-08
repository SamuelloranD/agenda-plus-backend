package com.agendaplus.identity.application.usecase;

import com.agendaplus.identity.application.port.SenhaEncoder;
import com.agendaplus.identity.domain.exception.EmailJaCadastradoException;
import com.agendaplus.identity.domain.model.Email;
import com.agendaplus.identity.domain.model.Role;
import com.agendaplus.identity.domain.model.Usuario;
import com.agendaplus.identity.domain.repository.UsuarioRepository;

import java.util.UUID;

public class CadastrarUsuarioUseCase {
    private final UsuarioRepository repository;
    private final SenhaEncoder senhas;

    public CadastrarUsuarioUseCase(UsuarioRepository repository, SenhaEncoder senhas) {
        this.repository = repository;
        this.senhas = senhas;
    }

    public Usuario executar(String nome, String email, String senha, Role role) {
        Email endereco = new Email(email);
        if (repository.buscarPorEmail(endereco).isPresent()) throw new EmailJaCadastradoException();
        return repository.salvar(new Usuario(UUID.randomUUID(), nome, endereco, senhas.codificar(senha), role));
    }
}
