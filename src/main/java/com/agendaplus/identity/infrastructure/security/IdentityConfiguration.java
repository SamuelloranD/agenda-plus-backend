package com.agendaplus.identity.infrastructure.security;

import com.agendaplus.identity.application.port.SenhaEncoder;
import com.agendaplus.identity.application.port.TokenGenerator;
import com.agendaplus.identity.application.usecase.AutenticarUsuarioUseCase;
import com.agendaplus.identity.application.usecase.CadastrarUsuarioUseCase;
import com.agendaplus.identity.domain.repository.UsuarioRepository;

import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityConfiguration {
    @Bean
    JwtService jwtService(@Value("${security.jwt.secret}") String segredo,
                          @Value("${security.jwt.expiration:PT1H}") Duration validade) {
        return new JwtService(segredo, validade, Clock.systemUTC());
    }

    @Bean
    CadastrarUsuarioUseCase cadastrarUsuarioUseCase(UsuarioRepository usuarios, SenhaEncoder senhas) {
        return new CadastrarUsuarioUseCase(usuarios, senhas);
    }

    @Bean
    AutenticarUsuarioUseCase autenticarUsuarioUseCase(UsuarioRepository usuarios, SenhaEncoder senhas, TokenGenerator tokens) {
        return new AutenticarUsuarioUseCase(usuarios, senhas, tokens);
    }
}
