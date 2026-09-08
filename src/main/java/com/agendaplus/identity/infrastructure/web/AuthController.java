package com.agendaplus.identity.infrastructure.web;

import com.agendaplus.identity.application.dto.CadastroRequest;
import com.agendaplus.identity.application.dto.LoginRequest;
import com.agendaplus.identity.application.dto.TokenAutenticacao;
import com.agendaplus.identity.application.dto.UsuarioResponse;
import com.agendaplus.identity.application.usecase.AutenticarUsuarioUseCase;
import com.agendaplus.identity.application.usecase.CadastrarUsuarioUseCase;

import com.agendaplus.identity.domain.model.Role;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final CadastrarUsuarioUseCase cadastrar;
    private final AutenticarUsuarioUseCase autenticar;

    public AuthController(CadastrarUsuarioUseCase cadastrar, AutenticarUsuarioUseCase autenticar) {
        this.cadastrar = cadastrar;
        this.autenticar = autenticar;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrarCliente(@Valid @RequestBody CadastroRequest request) {
        return cadastrar(request, Role.CLIENTE);
    }

    @PostMapping("/cadastro-negocio")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrarNegocio(@Valid @RequestBody CadastroRequest request) {
        return cadastrar(request, Role.ADMIN);
    }

    @PostMapping("/login")
    public TokenAutenticacao login(@Valid @RequestBody LoginRequest request) {
        return autenticar.executar(request.email(), request.senha());
    }

    private UsuarioResponse cadastrar(CadastroRequest request, Role role) {
        return UsuarioResponse.from(cadastrar.executar(request.nome(), request.email(), request.senha(), role));
    }
}
