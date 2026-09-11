package com.agendaplus.identity.infrastructure.persistence;

import com.agendaplus.identity.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioJpaEntity, UUID> {
    Optional<UsuarioJpaEntity> findByEmail(String email);
    List<UsuarioJpaEntity> findAllByRole(Role role);
}
