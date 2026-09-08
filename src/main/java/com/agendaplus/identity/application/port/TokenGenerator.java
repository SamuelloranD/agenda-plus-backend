package com.agendaplus.identity.application.port;

import com.agendaplus.identity.application.dto.TokenAutenticacao;

import com.agendaplus.identity.domain.model.Usuario;

public interface TokenGenerator {
    TokenAutenticacao gerar(Usuario usuario);
}
