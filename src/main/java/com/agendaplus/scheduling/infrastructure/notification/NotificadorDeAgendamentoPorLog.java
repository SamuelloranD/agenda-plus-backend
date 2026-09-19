package com.agendaplus.scheduling.infrastructure.notification;

import com.agendaplus.scheduling.application.port.NotificadorDeAgendamento;
import com.agendaplus.scheduling.domain.event.AgendamentoCancelado;
import com.agendaplus.scheduling.domain.event.AgendamentoConfirmado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificadorDeAgendamentoPorLog implements NotificadorDeAgendamento {
    private static final Logger logger = LoggerFactory.getLogger(NotificadorDeAgendamentoPorLog.class);

    @Override
    public void agendamentoConfirmado(AgendamentoConfirmado evento) {
        logger.info("Notificação de agendamento confirmado: agendamentoId={}, clienteId={}",
                evento.agendamentoId(), evento.clienteId());
    }

    @Override
    public void agendamentoCancelado(AgendamentoCancelado evento) {
        logger.info("Notificação de agendamento cancelado: agendamentoId={}, clienteId={}",
                evento.agendamentoId(), evento.clienteId());
    }
}
