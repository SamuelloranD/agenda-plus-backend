package com.agendaplus.scheduling.application.usecase;

import com.agendaplus.scheduling.application.dto.AgendamentoResponse;
import com.agendaplus.scheduling.application.dto.PaginaAgendamentosResponse;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class ListarAgendamentosUseCase {
    private final AgendamentoRepository repository;
    public ListarAgendamentosUseCase(AgendamentoRepository repository) { this.repository = repository; }
    public PaginaAgendamentosResponse executar(LocalDate dataInicio, LocalDate dataFim, UUID profissionalId, int pagina, int tamanho) {
        if (dataInicio == null || dataFim == null || dataFim.isBefore(dataInicio)) throw new IllegalArgumentException("O período de consulta é inválido.");
        validarPaginacao(pagina, tamanho);
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.plusDays(1).atTime(LocalTime.MIDNIGHT);
        var itens = repository.listar(inicio, fim, profissionalId, pagina, tamanho).stream().map(AgendamentoResponse::from).toList();
        long total = repository.contar(inicio, fim, profissionalId);
        return pagina(itens, pagina, tamanho, total);
    }

    public PaginaAgendamentosResponse executarParaCliente(UUID clienteId, LocalDate dataInicio, LocalDate dataFim,
                                                           int pagina, int tamanho) {
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("O período de consulta é inválido.");
        }
        validarPaginacao(pagina, tamanho);
        LocalDateTime inicio = dataInicio == null ? null : dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim == null ? null : dataFim.plusDays(1).atTime(LocalTime.MIDNIGHT);
        var itens = repository.listarPorCliente(clienteId, inicio, fim, pagina, tamanho).stream()
                .map(AgendamentoResponse::from).toList();
        long total = repository.contarPorCliente(clienteId, inicio, fim);
        return pagina(itens, pagina, tamanho, total);
    }

    private void validarPaginacao(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) throw new IllegalArgumentException("Paginação inválida.");
    }

    private PaginaAgendamentosResponse pagina(List<AgendamentoResponse> itens,
                                               int pagina, int tamanho, long total) {
        int paginas = (int) Math.ceil((double) total / tamanho);
        return new PaginaAgendamentosResponse(itens, pagina, tamanho, total, paginas);
    }
}
