package com.agendaplus.shared.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DinheiroTest {
    private static final Currency BRL = Currency.getInstance("BRL");

    @Test
    void normalizaValorParaDuasCasasDecimais() {
        var dinheiro = new Dinheiro(new BigDecimal("12.345"), BRL);

        assertThat(dinheiro.getValor()).isEqualByComparingTo("12.35");
    }

    @Test
    void rejeitaValorNegativo() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Dinheiro(new BigDecimal("-0.01"), BRL));
    }

    @Test
    void rejeitaValorOuMoedaNulos() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Dinheiro(null, BRL));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Dinheiro(BigDecimal.ONE, null));
    }

    @Test
    void consideraValorNormalizadoEMoedaNaIgualdade() {
        var primeiro = new Dinheiro(new BigDecimal("10"), BRL);
        var segundo = new Dinheiro(new BigDecimal("10.00"), BRL);

        assertThat(primeiro).isEqualTo(segundo);
        assertThat(primeiro.getMoeda()).isEqualTo(BRL);
    }
}
