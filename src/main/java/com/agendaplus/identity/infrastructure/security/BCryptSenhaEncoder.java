package com.agendaplus.identity.infrastructure.security;

import com.agendaplus.identity.application.port.SenhaEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptSenhaEncoder implements SenhaEncoder {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String codificar(String senha) {
        if (!valida(senha)) throw new IllegalArgumentException("Senha obrigatória, com no máximo 72 bytes UTF-8.");
        return encoder.encode(senha);
    }

    @Override
    public boolean confere(String senha, String hash) {
        return valida(senha) && encoder.matches(senha, hash);
    }

    private boolean valida(String senha) {
        return senha != null && !senha.isBlank() && senha.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
