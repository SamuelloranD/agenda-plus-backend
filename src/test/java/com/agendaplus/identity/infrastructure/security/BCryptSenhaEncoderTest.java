package com.agendaplus.identity.infrastructure.security;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class BCryptSenhaEncoderTest {
    private final BCryptSenhaEncoder encoder = new BCryptSenhaEncoder();

    @Test
    void hashTemSalEConfereApenasASenhaCorreta() {
        String primeiro = encoder.codificar("senha-teste");
        assertThat(encoder.codificar("senha-teste")).isNotEqualTo(primeiro);
        assertThat(encoder.confere("senha-teste", primeiro)).isTrue();
        assertThat(encoder.confere("senha-errada", primeiro)).isFalse();
    }

    @Test
    void respeitaLimiteDe72BytesSemTruncarSenhaUnicode() {
        String senha = "á".repeat(36);
        String hash = encoder.codificar(senha);
        assertThat(encoder.confere(senha, hash)).isTrue();
        assertThatIllegalArgumentException().isThrownBy(() -> encoder.codificar(senha + "a"));
        assertThat(encoder.confere(senha + "a", hash)).isFalse();
    }

    @Test
    void rejeitaSenhaAusenteOuEmBranco() {
        assertThatIllegalArgumentException().isThrownBy(() -> encoder.codificar(null));
        assertThatIllegalArgumentException().isThrownBy(() -> encoder.codificar(" "));
        assertThat(encoder.confere(null, "hash")).isFalse();
        assertThat(encoder.confere(" ", "hash")).isFalse();
    }
}
