package com.agendaplus.services.application;

public class ServicoComAgendamentosException extends RuntimeException {
    public ServicoComAgendamentosException() {
        super("Este serviço possui agendamentos e não pode ser excluído. Desative-o para impedir novas reservas.");
    }
}
