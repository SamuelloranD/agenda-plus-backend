package com.agendaplus.scheduling.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SpringDataAgendamentoRepository extends JpaRepository<AgendamentoJpaEntity, UUID> {
    @Query("""
            select a from AgendamentoJpaEntity a
            where a.profissionalId = :profissionalId
              and a.periodoInicio < :fim and a.periodoFim > :inicio
            """)
    List<AgendamentoJpaEntity> buscarConflitantes(@Param("profissionalId") UUID profissionalId,
                                                   @Param("inicio") LocalDateTime inicio,
                                                   @Param("fim") LocalDateTime fim);

    @Query("""
            select a from AgendamentoJpaEntity a
            where a.periodoInicio < :fim and a.periodoFim > :inicio
              and (:profissionalId is null or a.profissionalId = :profissionalId)
            order by a.periodoInicio
            """)
    Page<AgendamentoJpaEntity> listar(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim,
                                      @Param("profissionalId") UUID profissionalId, Pageable pageable);
}
