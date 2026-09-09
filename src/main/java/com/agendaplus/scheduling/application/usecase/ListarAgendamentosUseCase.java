package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.scheduling.application.dto.AgendamentoResponse;
import com.agendaplus.scheduling.application.dto.PaginaAgendamentosResponse;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public class ListarAgendamentosUseCase {
    private final AgendamentoRepository repository;
    public ListarAgendamentosUseCase(AgendamentoRepository repository) { this.repository = repository; }
    public PaginaAgendamentosResponse executar(LocalDate dataInicio, LocalDate dataFim, UUID profissionalId, int pagina, int tamanho) {
        if (dataInicio == null || dataFim == null || dataFim.isBefore(dataInicio)) throw new IllegalArgumentException("O período de consulta é inválido.");
        if (pagina < 0 || tamanho < 1 || tamanho > 100) throw new IllegalArgumentException("Paginação inválida.");
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.plusDays(1).atTime(LocalTime.MIDNIGHT);
        var itens = repository.listar(inicio, fim, profissionalId, pagina, tamanho).stream().map(AgendamentoResponse::from).toList();
        long total = repository.contar(inicio, fim, profissionalId);
        int paginas = (int) Math.ceil((double) total / tamanho);
        return new PaginaAgendamentosResponse(itens, pagina, tamanho, total, paginas);
    }
}
