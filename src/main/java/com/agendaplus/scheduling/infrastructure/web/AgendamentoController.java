package com.agendaplus.scheduling.infrastructure.web;

import com.agendaplus.scheduling.application.dto.*;
import com.agendaplus.scheduling.application.usecase.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/agendamentos")
@Tag(name = "Agendamentos", description = "Criação, consulta e mudança de status dos agendamentos")
public class AgendamentoController {
    private final CriarAgendamentoUseCase criar;
    private final CancelarAgendamentoUseCase cancelar;
    private final ConfirmarAgendamentoUseCase confirmar;
    private final ListarAgendamentosUseCase listar;

    public AgendamentoController(CriarAgendamentoUseCase criar, CancelarAgendamentoUseCase cancelar,
                                 ConfirmarAgendamentoUseCase confirmar, ListarAgendamentosUseCase listar) {
        this.criar = criar; this.cancelar = cancelar; this.confirmar = confirmar; this.listar = listar;
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar agendamento", description = "Cria um agendamento pendente após validar a disponibilidade do profissional")
    public AgendamentoResponse criar(@Valid @RequestBody CriarAgendamentoRequest r) {
        return AgendamentoResponse.from(criar.executar(r.inicio(), r.fim(), r.profissionalId(), r.clienteId(), r.servicoId()));
    }

    @PatchMapping("/{id}/confirmar")
    @Operation(summary = "Confirmar agendamento")
    public AgendamentoResponse confirmar(@PathVariable UUID id) { return AgendamentoResponse.from(confirmar.executar(id)); }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar agendamento", description = "Cancela somente quando faltam pelo menos 24 horas para o início")
    public AgendamentoResponse cancelar(@PathVariable UUID id) { return AgendamentoResponse.from(cancelar.executar(id)); }

    @GetMapping
    @Operation(summary = "Listar agendamentos", description = "Lista agendamentos por período e, opcionalmente, por profissional")
    public PaginaAgendamentosResponse listar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) UUID profissionalId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return listar.executar(dataInicio, dataFim, profissionalId, pagina, tamanho);
    }
}
