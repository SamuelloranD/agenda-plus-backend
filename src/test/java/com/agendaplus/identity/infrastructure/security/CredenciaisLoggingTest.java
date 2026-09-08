package com.agendaplus.identity.infrastructure.security;

import com.agendaplus.identity.application.dto.TokenAutenticacao;
import com.agendaplus.identity.application.dto.CadastroRequest;
import com.agendaplus.identity.application.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CredenciaisLoggingTest {
    @Test
    void representacaoTextualNaoExpoeSenhaOuToken() {
        assertThat(new CadastroRequest("Ana", "ana@exemplo.com", "segredo-nao-logavel").toString())
                .doesNotContain("segredo-nao-logavel");
        assertThat(new LoginRequest("ana@exemplo.com", "segredo-nao-logavel").toString())
                .doesNotContain("segredo-nao-logavel");
        assertThat(new TokenAutenticacao("jwt-nao-logavel", "Bearer", 3600).toString())
                .doesNotContain("jwt-nao-logavel");
    }
}
