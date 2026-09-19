package com.agendaplus.services.domain.model;

import com.agendaplus.shared.domain.model.Dinheiro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ServicoTest {
    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Currency BRL = Currency.getInstance("BRL");

    @Test
    void normalizaEAtualizaDadosDoServico() {
        var servico = new Servico(ID, " Corte ", 30, dinheiro("50.00"));

        assertThat(servico.getId()).isEqualTo(ID);
        assertThat(servico.getNome()).isEqualTo("Corte");
        assertThat(servico.getDuracaoMinutos()).isEqualTo(30);
        assertThat(servico.getPreco()).isEqualTo(dinheiro("50.00"));

        servico.atualizar(" Barba ", 45, dinheiro("35.00"));

        assertThat(servico.getNome()).isEqualTo("Barba");
        assertThat(servico.getDuracaoMinutos()).isEqualTo(45);
        assertThat(servico.getPreco()).isEqualTo(dinheiro("35.00"));
    }

    @Test
    void rejeitaNomeInvalido() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Servico(ID, null, 30, dinheiro("50")));
        assertThatIllegalArgumentException().isThrownBy(() -> new Servico(ID, " ", 30, dinheiro("50")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Servico(ID, "a".repeat(151), 30, dinheiro("50")));
    }

    @Test
    void rejeitaDuracaoNaoPositiva() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Servico(ID, "Corte", 0, dinheiro("50")));
        assertThatIllegalArgumentException().isThrownBy(() -> new Servico(ID, "Corte", -1, dinheiro("50")));
    }

    @Test
    void rejeitaPrecoAusente() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Servico(ID, "Corte", 30, null));
    }

    @Test
    void preservaCriacaoAoReaplicarCallbackEAtualizaModificacao() {
        var servico = new Servico(ID, "Corte", 30, dinheiro("50"));

        servico.onCreate();
        var criadoEm = servico.getCreatedAt();
        var atualizadoEm = servico.getUpdatedAt();
        servico.onCreate();

        assertThat(servico.getCreatedAt()).isEqualTo(criadoEm);
        assertThat(servico.getUpdatedAt()).isEqualTo(atualizadoEm);

        servico.onUpdate();

        assertThat(servico.getUpdatedAt()).isAfterOrEqualTo(atualizadoEm);
    }

    private Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor), BRL);
    }
}
