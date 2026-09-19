package com.agendaplus.scheduling.infrastructure.notification;

import com.agendaplus.scheduling.application.port.NotificadorDeAgendamento;
import com.agendaplus.scheduling.domain.event.AgendamentoCancelado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AgendamentoCanceladoListener {
    private static final Logger logger = LoggerFactory.getLogger(AgendamentoCanceladoListener.class);
    private final NotificadorDeAgendamento notificador;

    public AgendamentoCanceladoListener(NotificadorDeAgendamento notificador) {
        this.notificador = notificador;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCancelar(AgendamentoCancelado evento) {
        try {
            notificador.agendamentoCancelado(evento);
        } catch (Exception exception) {
            logger.error("Falha ao notificar agendamento cancelado: agendamentoId={}",
                    evento.agendamentoId(), exception);
        }
    }
}
