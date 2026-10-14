package com.agendaplus.professionals.infrastructure.persistence;

import com.agendaplus.professionals.domain.model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.Optional;

public interface ProfissionalRepository extends JpaRepository<Profissional, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Profissional p where p.id = :id")
    Optional<Profissional> findForScheduling(@Param("id") UUID id);
}
