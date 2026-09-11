package com.agendaplus.services.infrastructure.web;

import com.agendaplus.services.application.ServicoService;
import com.agendaplus.services.application.dto.ServicoRequest;
import com.agendaplus.services.application.dto.ServicoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/servicos")
public class ServicoController {
    private final ServicoService service;

    public ServicoController(ServicoService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoResponse criar(@Valid @RequestBody ServicoRequest request) { return ServicoResponse.from(service.criar(request)); }

    @GetMapping
    public List<ServicoResponse> listar() { return service.listar().stream().map(ServicoResponse::from).toList(); }

    @GetMapping("/{id}")
    public ServicoResponse buscar(@PathVariable UUID id) { return ServicoResponse.from(service.buscar(id)); }

    @PutMapping("/{id}")
    public ServicoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody ServicoRequest request) {
        return ServicoResponse.from(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) { service.excluir(id); }
}
