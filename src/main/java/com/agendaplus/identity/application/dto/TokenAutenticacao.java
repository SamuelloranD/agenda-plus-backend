package com.agendaplus.identity.application.dto;

public record TokenAutenticacao(String token, String tokenType, long expiresIn) {
    @Override
    public String toString() { return "TokenAutenticacao[token=PROTEGIDO]"; }
}
