package com.agendaplus.identity.application.usecase;

import com.agendaplus.identity.application.dto.TokenAutenticacao;
import com.agendaplus.identity.application.exception.CredenciaisInvalidasException;
import com.agendaplus.identity.application.port.SenhaEncoder;
import com.agendaplus.identity.application.port.TokenGenerator;
import com.agendaplus.identity.domain.model.Email;
import com.agendaplus.identity.domain.model.Usuario;
import com.agendaplus.identity.domain.repository.UsuarioRepository;

public class AutenticarUsuarioUseCase {
    private final UsuarioRepository repository;
    private final SenhaEncoder senhas;
    private final TokenGenerator tokens;
    private final String hashContaAusente;

    public AutenticarUsuarioUseCase(UsuarioRepository repository, SenhaEncoder senhas, TokenGenerator tokens) {
        this.repository = repository;
        this.senhas = senhas;
        this.tokens = tokens;
        // Mantém o custo do BCrypt também quando a conta não existe.
        this.hashContaAusente = senhas.codificar(java.util.UUID.randomUUID().toString());
    }

    public TokenAutenticacao executar(String email, String senha) {
        var usuario = repository.buscarPorEmail(new Email(email));
        boolean confere = senhas.confere(senha, usuario.map(Usuario::getSenhaHash).orElse(hashContaAusente));
        if (usuario.isEmpty() || !confere) throw new CredenciaisInvalidasException();
        return tokens.gerar(usuario.orElseThrow());
    }
}
