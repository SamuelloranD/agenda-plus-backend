package com.agendaplus.professionals.infrastructure.web;

import com.agendaplus.professionals.application.ProfissionalService;
import com.agendaplus.professionals.application.dto.ProfissionalRequest;
import com.agendaplus.professionals.application.dto.ProfissionalResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {
    private final ProfissionalService service;

    public ProfissionalController(ProfissionalService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfissionalResponse criar(@Valid @RequestBody ProfissionalRequest request) {
        return ProfissionalResponse.from(service.criar(request));
    }

    @GetMapping
    public List<ProfissionalResponse> listar() {
        return service.listar().stream().map(ProfissionalResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProfissionalResponse buscar(@PathVariable UUID id) { return ProfissionalResponse.from(service.buscar(id)); }

    @PutMapping("/{id}")
    public ProfissionalResponse atualizar(@PathVariable UUID id, @Valid @RequestBody ProfissionalRequest request) {
        return ProfissionalResponse.from(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) { service.excluir(id); }
}
