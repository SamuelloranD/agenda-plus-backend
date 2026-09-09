package com.agendaplus.scheduling.domain.exception;

public class CancelamentoNaoPermitidoException extends RuntimeException {
    public CancelamentoNaoPermitidoException() {
        super("Cancelamento não permitido com menos de 24 horas de antecedência.");
    }
}
