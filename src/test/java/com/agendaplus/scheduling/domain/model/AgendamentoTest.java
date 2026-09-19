package com.agendaplus.scheduling.domain.model;

import com.agendaplus.scheduling.domain.exception.CancelamentoNaoPermitidoException;
import com.agendaplus.scheduling.domain.exception.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AgendamentoTest {
    private final UUID profissionalId = UUID.randomUUID();
    private final UUID clienteId = UUID.randomUUID();
    private final UUID servicoId = UUID.randomUUID();
    private final LocalDateTime inicio = LocalDateTime.of(2026, 9, 10, 10, 0);

    private Agendamento novoAgendamento() {
        return new Agendamento(UUID.randomUUID(),
                new PeriodoAgendamento(inicio, inicio.plusHours(1)),
                profissionalId, clienteId, servicoId);
    }

    @Test
    void deveIniciarComoPendente() {
        assertEquals(StatusAgendamento.PENDENTE, novoAgendamento().getStatus());
    }

    @Test
    void devePermitirConfirmarAgendamentoPendente() {
        var agendamento = novoAgendamento();

        agendamento.confirmar();

        assertEquals(StatusAgendamento.CONFIRMADO, agendamento.getStatus());
    }

    @Test
    void devePermitirCancelarAgendamentoPendenteForaDaJanela() {
        var agendamento = novoAgendamento();

        agendamento.cancelar(inicio.minusHours(24));

        assertEquals(StatusAgendamento.CANCELADO, agendamento.getStatus());
    }

    @Test
    void devePermitirCancelarAgendamentoConfirmadoForaDaJanela() {
        var agendamento = novoAgendamento();
        agendamento.confirmar();

        agendamento.cancelar(inicio.minusHours(24));

        assertEquals(StatusAgendamento.CANCELADO, agendamento.getStatus());
    }

    @Test
    void devePermitirConcluirAgendamentoConfirmado() {
        var agendamento = novoAgendamento();
        agendamento.confirmar();

        agendamento.concluir();

        assertEquals(StatusAgendamento.CONCLUIDO, agendamento.getStatus());
    }

    @Test
    void deveRejeitarConfirmacaoDeAgendamentoCancelado() {
        var agendamento = novoAgendamento();
        agendamento.cancelar(inicio.minusHours(24));

        assertThrows(TransicaoInvalidaException.class, agendamento::confirmar);
    }

    @Test
    void deveRejeitarConfirmacaoDeAgendamentoJaConfirmado() {
        var agendamento = novoAgendamento();
        agendamento.confirmar();

        assertThrows(TransicaoInvalidaException.class, agendamento::confirmar);
    }

    @Test
    void deveRejeitarConclusaoDeAgendamentoPendente() {
        assertThrows(TransicaoInvalidaException.class, () -> novoAgendamento().concluir());
    }

    @Test
    void deveRejeitarConclusaoDeAgendamentoCancelado() {
        var agendamento = novoAgendamento();
        agendamento.cancelar(inicio.minusHours(24));

        assertThrows(TransicaoInvalidaException.class, agendamento::concluir);
    }

    @Test
    void deveRejeitarCancelamentoDeAgendamentoConcluido() {
        var agendamento = novoAgendamento();
        agendamento.confirmar();
        agendamento.concluir();

        assertThrows(TransicaoInvalidaException.class, () -> agendamento.cancelar(inicio.minusHours(24)));
    }

    @Test
    void deveRejeitarCancelamentoDentroDaJanelaDe24Horas() {
        var agendamento = novoAgendamento();

        assertThrows(CancelamentoNaoPermitidoException.class,
                () -> agendamento.cancelar(inicio.minusHours(23)));
    }

    @Test
    void deveReidratarAgendamentoComStatusPersistido() {
        var id = UUID.randomUUID();
        var periodo = new PeriodoAgendamento(inicio, inicio.plusHours(1));

        var agendamento = Agendamento.reidratar(id, periodo, profissionalId, clienteId,
                servicoId, StatusAgendamento.CANCELADO);

        assertEquals(id, agendamento.getId());
        assertEquals(StatusAgendamento.CANCELADO, agendamento.getStatus());
    }

    @Test
    void deveRejeitarReferenciasObrigatoriasAusentes() {
        var id = UUID.randomUUID();
        var periodo = new PeriodoAgendamento(inicio, inicio.plusHours(1));

        assertThrows(IllegalArgumentException.class,
                () -> new Agendamento(null, periodo, profissionalId, clienteId, servicoId));
        assertThrows(IllegalArgumentException.class,
                () -> new Agendamento(id, null, profissionalId, clienteId, servicoId));
        assertThrows(IllegalArgumentException.class,
                () -> new Agendamento(id, periodo, null, clienteId, servicoId));
        assertThrows(IllegalArgumentException.class,
                () -> new Agendamento(id, periodo, profissionalId, null, servicoId));
        assertThrows(IllegalArgumentException.class,
                () -> new Agendamento(id, periodo, profissionalId, clienteId, null));
    }

    @Test
    void deveRejeitarReidratacaoSemStatus() {
        var periodo = new PeriodoAgendamento(inicio, inicio.plusHours(1));

        assertThrows(IllegalArgumentException.class, () -> Agendamento.reidratar(
                UUID.randomUUID(), periodo, profissionalId, clienteId, servicoId, null));
    }
}
