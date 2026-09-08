package com.agendaplus.identity.domain.model;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.assertj.core.api.Assertions.*;

class UsuarioTest {
    private final UUID id = UUID.randomUUID();
    private final Email email = new Email("ana@exemplo.com");

    @ParameterizedTest
    @EnumSource(Role.class)
    void permiteOsTresPerfisComNomeNormalizado(Role role) {
        var usuario = new Usuario(id, " Ana Silva ", email, "hash-codificado", role);
        assertThat(usuario.getNome()).isEqualTo("Ana Silva");
        assertThat(usuario.getRole()).isEqualTo(role);
    }

    @Test
    void rejeitaDadosObrigatoriosAusentes() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Usuario(null, "Ana", email, "hash", Role.CLIENTE));
        assertThatIllegalArgumentException().isThrownBy(() -> new Usuario(id, " ", email, "hash", Role.CLIENTE));
        assertThatIllegalArgumentException().isThrownBy(() -> new Usuario(id, null, email, "hash", Role.CLIENTE));
        assertThatIllegalArgumentException().isThrownBy(() -> new Usuario(id, "Ana", null, "hash", Role.CLIENTE));
        assertThatIllegalArgumentException().isThrownBy(() -> new Usuario(id, "Ana", email, " ", Role.CLIENTE));
        assertThatIllegalArgumentException().isThrownBy(() -> new Usuario(id, "Ana", email, null, Role.CLIENTE));
        assertThatIllegalArgumentException().isThrownBy(() -> new Usuario(id, "Ana", email, "hash", null));
        assertThatIllegalArgumentException().isThrownBy(() -> new Usuario(id, "a".repeat(151), email, "hash", Role.CLIENTE));
    }
}
