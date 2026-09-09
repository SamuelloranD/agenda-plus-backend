package com.agendaplus.scheduling.domain.exception;

public class AgendamentoNaoEncontradoException extends RuntimeException {
    public AgendamentoNaoEncontradoException() {
        super("Agendamento não encontrado.");
    }
}
