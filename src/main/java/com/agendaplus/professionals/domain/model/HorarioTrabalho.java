package com.agendaplus.professionals.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "horarios_trabalho")
public class HorarioTrabalho {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;
    @Enumerated(EnumType.STRING)
    private DayOfWeek diaSemana;
    private LocalTime inicio;
    private LocalTime fim;

    protected HorarioTrabalho() {}

    public HorarioTrabalho(UUID id, DayOfWeek diaSemana, LocalTime inicio, LocalTime fim) {
        if (id == null || diaSemana == null || inicio == null || fim == null || !fim.isAfter(inicio)) {
            throw new IllegalArgumentException("Dia, início e fim válidos são obrigatórios.");
        }
        this.id = id;
        this.diaSemana = diaSemana;
        this.inicio = inicio;
        this.fim = fim;
    }

    void associar(Profissional profissional) { this.profissional = profissional; }

    public UUID getId() { return id; }
    public DayOfWeek getDiaSemana() { return diaSemana; }
    public LocalTime getInicio() { return inicio; }
    public LocalTime getFim() { return fim; }
}
