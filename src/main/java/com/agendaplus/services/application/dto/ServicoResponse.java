package com.agendaplus.services.application.dto;

import com.agendaplus.services.domain.model.Servico;

import java.util.UUID;

public record ServicoResponse(UUID id, String nome, int duracaoMinutos, DinheiroResponse preco) {
    public static ServicoResponse from(Servico servico) {
        return new ServicoResponse(servico.getId(), servico.getNome(), servico.getDuracaoMinutos(), DinheiroResponse.from(servico.getPreco()));
    }
}
