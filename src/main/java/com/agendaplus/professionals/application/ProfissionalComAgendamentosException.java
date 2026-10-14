package com.agendaplus.professionals.application;

public class ProfissionalComAgendamentosException extends RuntimeException {
    public ProfissionalComAgendamentosException() {
        super("Este profissional possui agendamentos. Desative o cadastro para preservar o histórico.");
    }
}
