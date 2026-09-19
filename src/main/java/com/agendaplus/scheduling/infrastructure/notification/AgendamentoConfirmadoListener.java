package com.agendaplus.scheduling.infrastructure.notification;

import com.agendaplus.scheduling.application.port.NotificadorDeAgendamento;
import com.agendaplus.scheduling.domain.event.AgendamentoConfirmado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AgendamentoConfirmadoListener {
    private static final Logger logger = LoggerFactory.getLogger(AgendamentoConfirmadoListener.class);
    private final NotificadorDeAgendamento notificador;

    public AgendamentoConfirmadoListener(NotificadorDeAgendamento notificador) {
        this.notificador = notificador;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoConfirmar(AgendamentoConfirmado evento) {
        try {
            notificador.agendamentoConfirmado(evento);
        } catch (Exception exception) {
            logger.error("Falha ao notificar agendamento confirmado: agendamentoId={}",
                    evento.agendamentoId(), exception);
        }
    }
}
