package com.agendaplus.identity.infrastructure.security;

import com.agendaplus.identity.application.dto.TokenAutenticacao;
import com.agendaplus.identity.application.port.TokenGenerator;
import com.agendaplus.identity.domain.model.Usuario;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

public class JwtService implements TokenGenerator {
    private static final String ISSUER = "agenda-plus";
    private final SecretKey chave;
    private final Duration validade;
    private final Clock clock;
    private final JwtParser parser;

    public JwtService(String segredo, Duration validade, Clock clock) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        if (validade.toSeconds() < 1) throw new IllegalArgumentException("Validade do JWT deve ser positiva.");
        this.validade = validade;
        this.clock = clock;
        this.parser = Jwts.parser().verifyWith(chave).requireIssuer(ISSUER)
                .clock(() -> Date.from(clock.instant())).build();
    }

    @Override
    public TokenAutenticacao gerar(Usuario usuario) {
        var agora = clock.instant();
        String token = Jwts.builder().issuer(ISSUER).subject(usuario.getId().toString())
                .claim("role", usuario.getRole().name()).issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(validade))).signWith(chave).compact();
        return new TokenAutenticacao(token, "Bearer", validade.toSeconds());
    }

    public UUID validar(String token) {
        var claims = parser.parseSignedClaims(token).getPayload();
        if (claims.getExpiration() == null) throw new IllegalArgumentException("Token sem expiração.");
        return UUID.fromString(claims.getSubject());
    }
}
