package com.agendaplus.professionals.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * O contexto professionals tem rigor DDD simplificado: a entidade JPA é usada diretamente
 * para evitar Mapper e Repository desacoplados sem perder a separação por contexto.
 */
@Entity
@Table(name = "profissionais")
public class Profissional {
    @Id
    private UUID id;
    private String nome;
    private String especialidade;

    @OneToMany(mappedBy = "profissional", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<HorarioTrabalho> horariosTrabalho = new ArrayList<>();

    protected Profissional() {}

    public Profissional(UUID id, String nome, String especialidade) {
        this.id = id;
        atualizarDados(nome, especialidade);
    }

    public void atualizarDados(String nome, String especialidade) {
        if (nome == null || nome.isBlank() || nome.strip().length() > 150) {
            throw new IllegalArgumentException("Nome deve conter entre 1 e 150 caracteres.");
        }
        if (especialidade == null || especialidade.isBlank() || especialidade.strip().length() > 150) {
            throw new IllegalArgumentException("Especialidade deve conter entre 1 e 150 caracteres.");
        }
        this.nome = nome.strip();
        this.especialidade = especialidade.strip();
    }

    public void substituirHorarios(List<HorarioTrabalho> horarios) {
        horariosTrabalho.clear();
        horarios.forEach(this::adicionarHorario);
    }

    private void adicionarHorario(HorarioTrabalho horario) {
        horario.associar(this);
        horariosTrabalho.add(horario);
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEspecialidade() { return especialidade; }
    public List<HorarioTrabalho> getHorariosTrabalho() { return List.copyOf(horariosTrabalho); }
}
