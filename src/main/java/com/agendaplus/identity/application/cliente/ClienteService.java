package com.agendaplus.identity.application.cliente;

import com.agendaplus.identity.application.dto.UsuarioResponse;
import com.agendaplus.identity.domain.exception.EmailJaCadastradoException;
import com.agendaplus.identity.domain.model.Email;
import com.agendaplus.identity.domain.model.Role;
import com.agendaplus.identity.domain.model.Usuario;
import com.agendaplus.identity.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ClienteService {
    private final UsuarioRepository usuarios;

    public ClienteService(UsuarioRepository usuarios) { this.usuarios = usuarios; }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.listarPorRole(Role.CLIENTE).stream()
                .sorted(Comparator.comparing(Usuario::getNome))
                .map(UsuarioResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Usuario buscar(UUID id) { return buscarCliente(id); }

    @Transactional
    public Usuario atualizar(UUID id, ClienteUpdateRequest request) {
        Usuario cliente = buscarCliente(id);
        Email email = new Email(request.email());
        usuarios.buscarPorEmail(email).filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> { throw new EmailJaCadastradoException(); });
        return usuarios.salvar(cliente.atualizarPerfil(request.nome(), email));
    }

    @Transactional
    public void excluir(UUID id) {
        buscarCliente(id);
        usuarios.excluir(id);
    }

    private Usuario buscarCliente(UUID id) {
        return usuarios.buscarPorId(id)
                .filter(usuario -> usuario.getRole() == Role.CLIENTE)
                .orElseThrow(ClienteNaoEncontradoException::new);
    }
}
