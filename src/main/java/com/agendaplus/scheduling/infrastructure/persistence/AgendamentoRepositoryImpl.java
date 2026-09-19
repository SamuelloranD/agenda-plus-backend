package com.agendaplus.scheduling.infrastructure.persistence;

import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.model.StatusAgendamento;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AgendamentoRepositoryImpl implements AgendamentoRepository {
    private final SpringDataAgendamentoRepository repository;
    private final AgendamentoMapper mapper;
    private final Clock clock;

    public AgendamentoRepositoryImpl(SpringDataAgendamentoRepository repository, AgendamentoMapper mapper, Clock clock) {
        this.repository = repository; this.mapper = mapper; this.clock = clock;
    }
    public Optional<Agendamento> buscarPorId(UUID id) { return repository.findById(id).map(mapper::toDomain); }
    public List<Agendamento> buscarPorProfissionalEPeriodo(UUID profissionalId, LocalDateTime inicio, LocalDateTime fim) {
        return repository.buscarConflitantes(profissionalId, inicio, fim).stream().map(mapper::toDomain).toList();
    }
    public List<Agendamento> listar(LocalDateTime inicio, LocalDateTime fim, UUID profissionalId, StatusAgendamento status, int pagina, int tamanho) {
        return repository.listar(inicio, fim, profissionalId, status,
                org.springframework.data.domain.PageRequest.of(pagina, tamanho)).map(mapper::toDomain).getContent();
    }
    public long contar(LocalDateTime inicio, LocalDateTime fim, UUID profissionalId, StatusAgendamento status) {
        return repository.listar(inicio, fim, profissionalId, status, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }
    public List<Agendamento> listarPorCliente(UUID clienteId, LocalDateTime inicio, LocalDateTime fim,
                                              int pagina, int tamanho) {
        return repository.listarPorCliente(clienteId, inicio, fim, LocalDateTime.now(clock),
                org.springframework.data.domain.PageRequest.of(pagina, tamanho)).stream().map(mapper::toDomain).toList();
    }
    public long contarPorCliente(UUID clienteId, LocalDateTime inicio, LocalDateTime fim) {
        return repository.contarPorCliente(clienteId, inicio, fim);
    }
    public Agendamento salvar(Agendamento agendamento) {
        AgendamentoJpaEntity entity = repository.findById(agendamento.getId()).map(existing -> {
            mapper.atualizar(existing, agendamento);
            return existing;
        }).orElseGet(() -> mapper.toEntity(agendamento));
        return mapper.toDomain(repository.saveAndFlush(entity));
    }
}
