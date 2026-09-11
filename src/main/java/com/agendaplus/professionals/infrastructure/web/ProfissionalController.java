package com.agendaplus.professionals.infrastructure.web;

import com.agendaplus.professionals.application.ProfissionalService;
import com.agendaplus.professionals.application.dto.ProfissionalRequest;
import com.agendaplus.professionals.application.dto.ProfissionalResponse;
import com.agendaplus.scheduling.application.dto.HorarioDisponivelResponse;
import com.agendaplus.scheduling.application.usecase.ListarHorariosDisponiveisUseCase;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {
    private final ProfissionalService service;
    private final ListarHorariosDisponiveisUseCase disponibilidade;

    public ProfissionalController(ProfissionalService service, ListarHorariosDisponiveisUseCase disponibilidade) {
        this.service = service;
        this.disponibilidade = disponibilidade;
    }

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

    @GetMapping("/{id}/horarios-disponiveis")
    public List<HorarioDisponivelResponse> horariosDisponiveis(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam UUID servicoId) {
        return disponibilidade.executar(id, data, servicoId);
    }

    @PutMapping("/{id}")
    public ProfissionalResponse atualizar(@PathVariable UUID id, @Valid @RequestBody ProfissionalRequest request) {
        return ProfissionalResponse.from(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) { service.excluir(id); }
}
