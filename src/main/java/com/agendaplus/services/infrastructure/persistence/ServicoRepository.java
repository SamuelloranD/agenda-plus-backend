package com.agendaplus.services.infrastructure.persistence;

import com.agendaplus.services.domain.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServicoRepository extends JpaRepository<Servico, UUID> {
}
