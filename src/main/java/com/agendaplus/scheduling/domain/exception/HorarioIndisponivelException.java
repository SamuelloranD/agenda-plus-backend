package com.agendaplus.scheduling.domain.exception;

public class HorarioIndisponivelException extends RuntimeException {
    public HorarioIndisponivelException() {
        super("O horário escolhido não está disponível.");
    }
}
