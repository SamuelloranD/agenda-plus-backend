package com.agendaplus.identity.infrastructure.security;

import com.agendaplus.identity.domain.model.Email;
import com.agendaplus.identity.domain.model.Role;
import com.agendaplus.identity.domain.model.Usuario;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET = "segredo-exclusivo-para-testes-unitarios-123456789";
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-07T12:00:00Z"), ZoneOffset.UTC);
    private final JwtService service = new JwtService(SECRET, Duration.ofHours(1), clock);
    private final Usuario usuario = new Usuario(UUID.randomUUID(), "Ana", new Email("ana@exemplo.com"), "hash", Role.CLIENTE);

    @Test
    void validaTokenAssinadoParaOUsuario() {
        var token = service.gerar(usuario);
        assertThat(service.validar(token.token())).isEqualTo(usuario.getId());
        assertThat(token.expiresIn()).isEqualTo(3600);
    }

    @Test
    void rejeitaTokenExpiradoSemEsperarRelogioReal() {
        var futuro = new JwtService(SECRET, Duration.ofHours(1), Clock.offset(clock, Duration.ofHours(2)));
        assertThatThrownBy(() -> futuro.validar(service.gerar(usuario).token())).isInstanceOf(JwtException.class);
    }

    @Test
    void rejeitaAssinaturaDeOutraChave() {
        var outro = new JwtService("outra-chave-exclusiva-para-testes-987654321", Duration.ofHours(1), clock);
        assertThatThrownBy(() -> service.validar(outro.gerar(usuario).token())).isInstanceOf(JwtException.class);
    }

    @Test
    void rejeitaSegredoCurtoEValidadeNaoPositiva() {
        assertThatThrownBy(() -> new JwtService("curto", Duration.ofHours(1), clock))
                .isInstanceOf(io.jsonwebtoken.security.WeakKeyException.class);
        assertThatIllegalArgumentException().isThrownBy(() -> new JwtService(SECRET, Duration.ZERO, clock));
    }
}
