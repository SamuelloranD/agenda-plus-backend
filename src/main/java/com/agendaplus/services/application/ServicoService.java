package com.agendaplus.services.application;

import com.agendaplus.services.application.dto.ServicoRequest;
import com.agendaplus.services.domain.model.Servico;
import com.agendaplus.services.infrastructure.persistence.ServicoRepository;
import com.agendaplus.shared.domain.model.Dinheiro;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Service
public class ServicoService {
    private final ServicoRepository servicos;

    public ServicoService(ServicoRepository servicos) { this.servicos = servicos; }

    @Transactional
    public Servico criar(ServicoRequest request) {
        var servico = new Servico(UUID.randomUUID(), request.nome(), request.duracaoMinutos(), dinheiro(request));
        return servicos.saveAndFlush(servico);
    }

    @Transactional(readOnly = true)
    public List<Servico> listar() { return servicos.findAll().stream().sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome())).toList(); }

    @Transactional(readOnly = true)
    public Servico buscar(UUID id) { return servicos.findById(id).orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado.")); }

    @Transactional
    public Servico atualizar(UUID id, ServicoRequest request) {
        var servico = buscar(id);
        servico.atualizar(request.nome(), request.duracaoMinutos(), dinheiro(request));
        return servicos.saveAndFlush(servico);
    }

    @Transactional
    public void excluir(UUID id) {
        if (!servicos.existsById(id)) throw new IllegalArgumentException("Serviço não encontrado.");
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
