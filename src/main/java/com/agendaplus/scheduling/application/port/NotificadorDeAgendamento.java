package com.agendaplus.scheduling.application.port;

import com.agendaplus.scheduling.domain.event.AgendamentoCancelado;
import com.agendaplus.scheduling.domain.event.AgendamentoConfirmado;

public interface NotificadorDeAgendamento {
    void agendamentoConfirmado(AgendamentoConfirmado evento);
    void agendamentoCancelado(AgendamentoCancelado evento);
}
