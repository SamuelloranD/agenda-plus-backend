package com.agendaplus.scheduling.infrastructure.persistence;

import com.agendaplus.scheduling.domain.model.Agendamento;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AgendamentoRepositoryImpl implements AgendamentoRepository {
    private final SpringDataAgendamentoRepository repository;
    private final AgendamentoMapper mapper;

    public AgendamentoRepositoryImpl(SpringDataAgendamentoRepository repository, AgendamentoMapper mapper) {
        this.repository = repository; this.mapper = mapper;
    }
    public Optional<Agendamento> buscarPorId(UUID id) { return repository.findById(id).map(mapper::toDomain); }
    public List<Agendamento> buscarPorProfissionalEPeriodo(UUID profissionalId, LocalDateTime inicio, LocalDateTime fim) {
        return repository.buscarConflitantes(profissionalId, inicio, fim).stream().map(mapper::toDomain).toList();
    }
    public List<Agendamento> listar(LocalDateTime inicio, LocalDateTime fim, UUID profissionalId, int pagina, int tamanho) {
        return repository.listar(inicio, fim, profissionalId,
                org.springframework.data.domain.PageRequest.of(pagina, tamanho)).map(mapper::toDomain).getContent();
    }
    public long contar(LocalDateTime inicio, LocalDateTime fim, UUID profissionalId) {
        return repository.listar(inicio, fim, profissionalId, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }
    public Agendamento salvar(Agendamento agendamento) { return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(agendamento))); }
}
