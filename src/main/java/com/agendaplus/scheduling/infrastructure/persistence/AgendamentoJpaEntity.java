package com.agendaplus.scheduling.infrastructure.persistence;

import com.agendaplus.scheduling.domain.model.StatusAgendamento;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agendamentos")
public class AgendamentoJpaEntity {
    @Id private UUID id;
    @Column(name = "periodo_inicio", nullable = false) private LocalDateTime periodoInicio;
    @Column(name = "periodo_fim", nullable = false) private LocalDateTime periodoFim;
    @Column(name = "profissional_id", nullable = false) private UUID profissionalId;
    @Column(name = "cliente_id", nullable = false) private UUID clienteId;
    @Column(name = "servico_id", nullable = false) private UUID servicoId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private StatusAgendamento status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected AgendamentoJpaEntity() {}

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public AgendamentoJpaEntity(UUID id, LocalDateTime periodoInicio, LocalDateTime periodoFim,
                                UUID profissionalId, UUID clienteId, UUID servicoId, StatusAgendamento status) {
        this.id = id; this.periodoInicio = periodoInicio; this.periodoFim = periodoFim;
        this.profissionalId = profissionalId; this.clienteId = clienteId; this.servicoId = servicoId; this.status = status;
    }
    public UUID getId() { return id; }
    public LocalDateTime getPeriodoInicio() { return periodoInicio; }
    public LocalDateTime getPeriodoFim() { return periodoFim; }
    public UUID getProfissionalId() { return profissionalId; }
    public UUID getClienteId() { return clienteId; }
    public UUID getServicoId() { return servicoId; }
    public StatusAgendamento getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void atualizarStatus(StatusAgendamento status) { this.status = status; }
    public void atualizar(LocalDateTime inicio, LocalDateTime fim, UUID profissionalId, UUID clienteId,
                          UUID servicoId, StatusAgendamento status) {
        this.periodoInicio = inicio; this.periodoFim = fim; this.profissionalId = profissionalId;
        this.clienteId = clienteId; this.servicoId = servicoId; this.status = status;
    }
}
