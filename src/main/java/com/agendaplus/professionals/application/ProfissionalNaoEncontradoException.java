package com.agendaplus.professionals.application;

public class ProfissionalNaoEncontradoException extends RuntimeException {
    public ProfissionalNaoEncontradoException() {
        super("Profissional não encontrado.");
    }
}
