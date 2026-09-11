package com.agendaplus.professionals.infrastructure.persistence;

import com.agendaplus.professionals.domain.model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfissionalRepository extends JpaRepository<Profissional, UUID> {
}
