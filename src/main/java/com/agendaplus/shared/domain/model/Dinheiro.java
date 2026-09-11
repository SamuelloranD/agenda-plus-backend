package com.agendaplus.shared.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public final class Dinheiro {
    private static final int CASAS_DECIMAIS = 2;

    private final BigDecimal valor;
    private final Currency moeda;

    public Dinheiro(BigDecimal valor, Currency moeda) {
        if (valor == null || moeda == null) {
            throw new IllegalArgumentException("Valor e moeda são obrigatórios.");
        }
        if (valor.signum() < 0) {
            throw new IllegalArgumentException("O valor não pode ser negativo.");
        }
        this.valor = valor.setScale(CASAS_DECIMAIS, RoundingMode.HALF_UP);
        this.moeda = moeda;
    }

    public BigDecimal getValor() { return valor; }
    public Currency getMoeda() { return moeda; }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Dinheiro dinheiro)) return false;
        return valor.equals(dinheiro.valor) && moeda.equals(dinheiro.moeda);
    }

    @Override
    public int hashCode() { return Objects.hash(valor, moeda); }
}
