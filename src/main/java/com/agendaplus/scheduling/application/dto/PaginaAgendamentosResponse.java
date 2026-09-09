package com.agendaplus.scheduling.application.dto;

import java.util.List;

public record PaginaAgendamentosResponse(List<AgendamentoResponse> conteudo, int pagina, int tamanho, long totalElementos,
                                         int totalPaginas) {}
