package com.agendaplus.identity.application.port;

public interface SenhaEncoder {
    String codificar(String senha);
    boolean confere(String senha, String hash);
}
