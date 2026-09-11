package com.agendaplus.identity.infrastructure.web;

import com.agendaplus.identity.application.cliente.ClienteService;
import com.agendaplus.identity.application.cliente.ClienteUpdateRequest;
import com.agendaplus.identity.application.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) { this.service = service; }

    @GetMapping
    public List<UsuarioResponse> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable UUID id) { return UsuarioResponse.from(service.buscar(id)); }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable UUID id, @Valid @RequestBody ClienteUpdateRequest request) {
        return UsuarioResponse.from(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) { service.excluir(id); }
}
