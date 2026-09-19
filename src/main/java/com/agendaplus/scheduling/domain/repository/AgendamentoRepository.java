package com.agendaplus.scheduling.domain.repository;

import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.model.StatusAgendamento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgendamentoRepository {
    Optional<Agendamento> buscarPorId(UUID id);
    List<Agendamento> buscarPorProfissionalEPeriodo(UUID profissionalId, LocalDateTime inicio, LocalDateTime fim);
    List<Agendamento> listar(LocalDateTime inicio, LocalDateTime fim, UUID profissionalId, StatusAgendamento status, int pagina, int tamanho);
    long contar(LocalDateTime inicio, LocalDateTime fim, UUID profissionalId, StatusAgendamento status);
    List<Agendamento> listarPorCliente(UUID clienteId, LocalDateTime inicio, LocalDateTime fim, int pagina, int tamanho);
    long contarPorCliente(UUID clienteId, LocalDateTime inicio, LocalDateTime fim);
    Agendamento salvar(Agendamento agendamento);
}
