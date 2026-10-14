package com.agendaplus.services.application;

import com.agendaplus.services.application.dto.ServicoRequest;
import com.agendaplus.services.domain.model.Servico;
import com.agendaplus.services.infrastructure.persistence.ServicoRepository;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import com.agendaplus.shared.domain.model.Dinheiro;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Service
public class ServicoService {
    private final ServicoRepository servicos;
    private final AgendamentoRepository agendamentos;

    public ServicoService(ServicoRepository servicos, AgendamentoRepository agendamentos) {
        this.servicos = servicos;
        this.agendamentos = agendamentos;
    }

    @Transactional
    public Servico criar(ServicoRequest request) {
        var servico = new Servico(UUID.randomUUID(), request.nome(), request.duracaoMinutos(), dinheiro(request));
        servico.atualizar(request.nome(), request.descricao(), request.duracaoMinutos(), servico.getPreco());
        servico.definirAtivo(request.ativo() == null || request.ativo());
        return servicos.saveAndFlush(servico);
    }

    @Transactional(readOnly = true)
    public List<Servico> listar() { return servicos.findAll().stream().sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome())).toList(); }

    @Transactional(readOnly = true)
    public Servico buscar(UUID id) { return servicos.findById(id).orElseThrow(ServicoNaoEncontradoException::new); }

    @Transactional
    public Servico atualizar(UUID id, ServicoRequest request) {
        var servico = buscar(id);
        servico.atualizar(request.nome(), request.descricao() == null ? servico.getDescricao() : request.descricao(), request.duracaoMinutos(), dinheiro(request));
        if (request.ativo() != null) servico.definirAtivo(request.ativo());
        return servicos.saveAndFlush(servico);
    }

    @Transactional
    public Servico alterarStatus(UUID id, boolean ativo) {
        var servico = buscar(id);
        servico.definirAtivo(ativo);
        return servicos.saveAndFlush(servico);
    }

    @Transactional
    public void excluir(UUID id) {
        var servico = buscar(id);
        if (agendamentos.temVinculosServicoAtivosOuFuturos(id)) throw new ServicoComAgendamentosException();
        if (agendamentos.temVinculosServico(id)) {
            servico.definirAtivo(false);
            servicos.saveAndFlush(servico);
            return;
        }
        servicos.deleteById(id);
    }

    private Dinheiro dinheiro(ServicoRequest request) {
        try {
            return new Dinheiro(request.preco().valor(), Currency.getInstance(request.preco().moeda().toUpperCase()));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Moeda inválida.", exception);
        }
    }
}
