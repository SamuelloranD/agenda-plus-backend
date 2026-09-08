package com.agendaplus.identity.infrastructure.web;

import com.agendaplus.identity.application.dto.UsuarioResponse;
import com.agendaplus.identity.domain.repository.UsuarioRepository;

import com.agendaplus.identity.application.exception.CredenciaisInvalidasException;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/identity")
public class IdentityController {
    private final UsuarioRepository usuarios;

    public IdentityController(UsuarioRepository usuarios) { this.usuarios = usuarios; }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal UUID id) {
        return usuarios.buscarPorId(id).map(UsuarioResponse::from).orElseThrow(CredenciaisInvalidasException::new);
    }
}
