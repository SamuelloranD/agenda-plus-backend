package com.agendaplus.services.application.dto;

import com.agendaplus.shared.domain.model.Dinheiro;

import java.math.BigDecimal;

public record DinheiroResponse(BigDecimal valor, String moeda) {
    public static DinheiroResponse from(Dinheiro dinheiro) {
        return new DinheiroResponse(dinheiro.getValor(), dinheiro.getMoeda().getCurrencyCode());
    }
}
