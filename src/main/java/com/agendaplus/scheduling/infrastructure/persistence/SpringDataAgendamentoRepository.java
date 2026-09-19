package com.agendaplus.scheduling.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.agendaplus.scheduling.domain.model.StatusAgendamento;
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
              and (:status is null or a.status = :status)
            order by a.periodoInicio
            """)
    Page<AgendamentoJpaEntity> listar(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim,
                                      @Param("profissionalId") UUID profissionalId, @Param("status") StatusAgendamento status,
                                      Pageable pageable);

    @Query("""
            select a from AgendamentoJpaEntity a
            where a.clienteId = :clienteId
              and (cast(:inicio as LocalDateTime) is null or a.periodoFim > :inicio)
              and (cast(:fim as LocalDateTime) is null or a.periodoInicio < :fim)
            order by
              case when a.status in (PENDENTE, CONFIRMADO) and a.periodoInicio >= :agora then 0 else 1 end,
              case when a.status in (PENDENTE, CONFIRMADO) and a.periodoInicio >= :agora then a.periodoInicio else null end asc,
              case when a.status in (PENDENTE, CONFIRMADO) and a.periodoInicio >= :agora then null else a.periodoInicio end desc,
              a.id asc
            """)
    List<AgendamentoJpaEntity> listarPorCliente(@Param("clienteId") UUID clienteId,
                                                @Param("inicio") LocalDateTime inicio,
                                                @Param("fim") LocalDateTime fim,
                                                @Param("agora") LocalDateTime agora,
                                                Pageable pageable);

    @Query("""
            select count(a) from AgendamentoJpaEntity a
            where a.clienteId = :clienteId
              and (cast(:inicio as LocalDateTime) is null or a.periodoFim > :inicio)
              and (cast(:fim as LocalDateTime) is null or a.periodoInicio < :fim)
            """)
    long contarPorCliente(@Param("clienteId") UUID clienteId,
                          @Param("inicio") LocalDateTime inicio,
                          @Param("fim") LocalDateTime fim);
}
