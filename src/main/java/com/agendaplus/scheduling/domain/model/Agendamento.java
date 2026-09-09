package com.agendaplus.scheduling.domain.model;

import com.agendaplus.scheduling.domain.exception.CancelamentoNaoPermitidoException;
import com.agendaplus.scheduling.domain.exception.TransicaoInvalidaException;

import java.time.LocalDateTime;
import java.util.UUID;

public final class Agendamento {
    private final UUID id;
    private final PeriodoAgendamento periodo;
    private final UUID profissionalId;
    private final UUID clienteId;
    private final UUID servicoId;
    private StatusAgendamento status;

    public Agendamento(UUID id, PeriodoAgendamento periodo, UUID profissionalId,
                       UUID clienteId, UUID servicoId) {
        if (id == null || periodo == null || profissionalId == null || clienteId == null || servicoId == null) {
            throw new IllegalArgumentException("Id, período e referências do agendamento são obrigatórios.");
        }
        this.id = id;
        this.periodo = periodo;
        this.profissionalId = profissionalId;
        this.clienteId = clienteId;
        this.servicoId = servicoId;
        this.status = StatusAgendamento.PENDENTE;
    }

    public static Agendamento reidratar(UUID id, PeriodoAgendamento periodo, UUID profissionalId,
                                        UUID clienteId, UUID servicoId, StatusAgendamento status) {
        if (status == null) throw new IllegalArgumentException("Status do agendamento é obrigatório.");
        var agendamento = new Agendamento(id, periodo, profissionalId, clienteId, servicoId);
        agendamento.status = status;
        return agendamento;
    }

    public void confirmar() {
        exigirStatus(StatusAgendamento.PENDENTE);
        status = StatusAgendamento.CONFIRMADO;
    }

    public void cancelar(LocalDateTime agora) {
        if (periodo.dentroDaJanelaDeCancelamento(agora)) {
            throw new CancelamentoNaoPermitidoException();
        }
        if (status != StatusAgendamento.PENDENTE && status != StatusAgendamento.CONFIRMADO) {
            throw new TransicaoInvalidaException("Apenas agendamentos pendentes ou confirmados podem ser cancelados.");
        }
        status = StatusAgendamento.CANCELADO;
    }

    public void concluir() {
        exigirStatus(StatusAgendamento.CONFIRMADO);
        status = StatusAgendamento.CONCLUIDO;
    }

    private void exigirStatus(StatusAgendamento statusEsperado) {
        if (status != statusEsperado) {
            throw new TransicaoInvalidaException("Transição inválida a partir do status " + status + ".");
        }
    }

    public UUID getId() { return id; }
    public PeriodoAgendamento getPeriodo() { return periodo; }
    public StatusAgendamento getStatus() { return status; }
    public UUID getProfissionalId() { return profissionalId; }
    public UUID getClienteId() { return clienteId; }
    public UUID getServicoId() { return servicoId; }
}
