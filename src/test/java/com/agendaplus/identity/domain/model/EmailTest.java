package com.agendaplus.identity.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class EmailTest {
    @Test
    void normalizaEmailParaIdentificarAConta() {
        assertThat(new Email(" Ana@Exemplo.com ")).isEqualTo(new Email("ana@exemplo.com"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "ana", "@exemplo.com", "ana@", "ana@@exemplo.com", "ana a@exemplo.com", "ana@exemplo", ".ana@exemplo.com", "ana..silva@exemplo.com", "ana@-exemplo.com"})
    void rejeitaFormatoInvalido(String valor) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Email(valor));
    }

    @Test
    void rejeitaEnderecosQueExcedemOsLimitesDeTamanho() {
        var parteLocalCom65Caracteres = "a".repeat(65) + "@exemplo.com";
        var enderecoCom255Caracteres = "a@" + "b".repeat(249) + ".com";

        assertThatIllegalArgumentException().isThrownBy(() -> new Email(parteLocalCom65Caracteres));
        assertThatIllegalArgumentException().isThrownBy(() -> new Email(enderecoCom255Caracteres));
    }
}
