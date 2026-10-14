package com.agendaplus.professionals.application;

import com.agendaplus.professionals.application.dto.HorarioTrabalhoRequest;
import com.agendaplus.professionals.application.dto.ProfissionalRequest;
import com.agendaplus.professionals.domain.model.HorarioTrabalho;
import com.agendaplus.professionals.domain.model.Profissional;
import com.agendaplus.professionals.infrastructure.persistence.ProfissionalRepository;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ProfissionalService {
    private final ProfissionalRepository profissionais;
    private final AgendamentoRepository agendamentos;

    public ProfissionalService(ProfissionalRepository profissionais, AgendamentoRepository agendamentos) {
        this.profissionais = profissionais;
        this.agendamentos = agendamentos;
    }

    @Transactional
    public Profissional criar(ProfissionalRequest request) {
        request.validarHorarios();
        var profissional = new Profissional(UUID.randomUUID(), request.nome(), request.especialidade(), request.imagem());
        profissional.definirAtivo(!Boolean.FALSE.equals(request.ativo()));
        profissional.substituirHorarios(criarHorarios(request.horariosTrabalho()));
        return profissionais.saveAndFlush(profissional);
    }

    @Transactional(readOnly = true)
    public List<Profissional> listar() { return profissionais.findAll().stream().sorted(Comparator.comparing(Profissional::getNome)).toList(); }

    @Transactional(readOnly = true)
    public Profissional buscar(UUID id) { return profissionais.findById(id).orElseThrow(ProfissionalNaoEncontradoException::new); }

    @Transactional
    public Profissional atualizar(UUID id, ProfissionalRequest request) {
        request.validarHorarios();
        var profissional = buscar(id);
        profissional.atualizarDados(request.nome(), request.especialidade(), request.imagem());
        if (request.ativo() != null) profissional.definirAtivo(request.ativo());
        profissional.atualizarHorarios(criarHorarios(request.horariosTrabalho()));
        return profissionais.saveAndFlush(profissional);
    }

    @Transactional
    public void excluir(UUID id) {
        if (!profissionais.existsById(id)) throw new ProfissionalNaoEncontradoException();
        if (agendamentos.temVinculosProfissional(id)) throw new ProfissionalComAgendamentosException();
        profissionais.deleteById(id);
    }

    @Transactional
    public Profissional alterarStatus(UUID id, boolean ativo) {
        var profissional = buscar(id);
        profissional.definirAtivo(ativo);
        return profissionais.saveAndFlush(profissional);
    }

    private List<HorarioTrabalho> criarHorarios(List<HorarioTrabalhoRequest> requests) {
        return requests.stream().map(request -> new HorarioTrabalho(UUID.randomUUID(), request.diaSemana(), request.inicio(), request.fim())).toList();
    }
}
