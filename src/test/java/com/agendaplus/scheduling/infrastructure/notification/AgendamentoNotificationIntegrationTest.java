package com.agendaplus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.agendaplus.scheduling.domain.event.AgendamentoCancelado;
import com.agendaplus.scheduling.domain.event.AgendamentoConfirmado;
import com.agendaplus.scheduling.domain.model.PeriodoAgendamento;
import com.agendaplus.scheduling.application.port.NotificadorDeAgendamento;
import com.agendaplus.scheduling.infrastructure.notification.AgendamentoCanceladoListener;
import com.agendaplus.scheduling.infrastructure.notification.AgendamentoConfirmadoListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, AgendamentoNotificationIntegrationTest.FakeConfig.class})
class AgendamentoNotificationIntegrationTest {
    @org.springframework.beans.factory.annotation.Autowired ApplicationEventPublisher publisher;
    @org.springframework.beans.factory.annotation.Autowired TransactionTemplate transactions;
    @org.springframework.beans.factory.annotation.Autowired SpyNotificador spy;
    @org.springframework.beans.factory.annotation.Autowired MockMvc mvc;
    @org.springframework.beans.factory.annotation.Autowired ObjectMapper json;
    @org.springframework.beans.factory.annotation.Autowired Clock clock;

    @BeforeEach
    void limpar() {
        spy.reset();
    }

    @Test
    void confirmacaoExecutaSincronamenteSomenteDepoisDoCommit() {
        Thread threadPublicadora = Thread.currentThread();

        transactions.executeWithoutResult(status -> {
            publisher.publishEvent(confirmado());
            assertEquals(0, spy.confirmados.get());
        });

        assertAll(
                () -> assertEquals(1, spy.confirmados.get()),
                () -> assertSame(threadPublicadora, spy.threadConfirmacao.get())
        );
    }

    @Test
    void cancelamentoExecutaSincronamenteSomenteDepoisDoCommit() {
        Thread threadPublicadora = Thread.currentThread();

        transactions.executeWithoutResult(status -> {
            publisher.publishEvent(cancelado());
            assertEquals(0, spy.cancelados.get());
        });

        assertAll(
                () -> assertEquals(1, spy.cancelados.get()),
                () -> assertSame(threadPublicadora, spy.threadCancelamento.get())
        );
    }

    @Test
    void listenersNaoExecutamDepoisDeRollback() {
        transactions.executeWithoutResult(status -> {
            publisher.publishEvent(confirmado());
            publisher.publishEvent(cancelado());
            status.setRollbackOnly();
        });

        assertAll(
                () -> assertEquals(0, spy.confirmados.get()),
                () -> assertEquals(0, spy.cancelados.get())
        );
    }

    @Test
    void falhaDoNotificadorDeConfirmacaoNaoPropaga() {
        var listener = new AgendamentoConfirmadoListener(new NotificadorQueFalha());

        assertDoesNotThrow(() -> listener.aoConfirmar(confirmado()));
    }

    @Test
    void falhaDoNotificadorDeCancelamentoNaoPropaga() {
        var listener = new AgendamentoCanceladoListener(new NotificadorQueFalha());

        assertDoesNotThrow(() -> listener.aoCancelar(cancelado()));
    }

    @Test
    void falhaDoNotificadorNaoAlteraRespostaDeConfirmacao() throws Exception {
        Conta cliente = cliente();
        Conta administrador = administrador();
        String agendamentoId = criarAgendamento(cliente, LocalDateTime.now(clock).plusDays(3));
        spy.falharConfirmacao = true;

        mvc.perform(patch("/agendamentos/{id}/confirmar", agendamentoId)
                        .header("Authorization", "Bearer " + administrador.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("CONFIRMADO"));
        assertEquals(1, spy.confirmados.get());
    }

    @Test
    void falhaDoNotificadorNaoAlteraRespostaDeCancelamento() throws Exception {
        Conta cliente = cliente();
        String agendamentoId = criarAgendamento(cliente, LocalDateTime.now(clock).plusDays(3));
        spy.falharCancelamento = true;

        mvc.perform(patch("/agendamentos/{id}/cancelar", agendamentoId)
                        .header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("CANCELADO"));
        assertEquals(1, spy.cancelados.get());
    }

    private AgendamentoConfirmado confirmado() {
        return new AgendamentoConfirmado(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), periodo());
    }

    private AgendamentoCancelado cancelado() {
        return new AgendamentoCancelado(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), periodo());
    }

    private PeriodoAgendamento periodo() {
        return new PeriodoAgendamento(LocalDateTime.of(2026, 9, 20, 14, 0), LocalDateTime.of(2026, 9, 20, 15, 0));
    }

    private String criarAgendamento(Conta cliente, LocalDateTime inicio) throws Exception {
        var request = Map.of(
                "inicio", inicio,
                "fim", inicio.plusHours(1),
                "profissionalId", UUID.randomUUID(),
                "clienteId", cliente.id(),
                "servicoId", UUID.randomUUID());

        String resposta = mvc.perform(post("/agendamentos")
                        .header("Authorization", "Bearer " + cliente.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("id").asText();
    }

    private Conta cliente() throws Exception {
        return conta("/auth/cadastro");
    }

    private Conta administrador() throws Exception {
        return conta("/auth/cadastro-negocio");
    }

    private Conta conta(String rotaCadastro) throws Exception {
        String email = UUID.randomUUID() + "@exemplo.com";
        String cadastro = mvc.perform(post(rotaCadastro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of(
                                "nome", "Cliente",
                                "email", email,
                                "senha", "senha-segura"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String login = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of(
                                "email", email,
                                "senha", "senha-segura"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode usuario = json.readTree(cadastro);
        JsonNode autenticacao = json.readTree(login);
        return new Conta(UUID.fromString(usuario.get("id").asText()), autenticacao.get("token").asText());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FakeConfig {
        @Bean @Primary SpyNotificador spyNotificador() { return new SpyNotificador(); }
    }

    static class SpyNotificador implements NotificadorDeAgendamento {
        final AtomicInteger confirmados = new AtomicInteger();
        final AtomicInteger cancelados = new AtomicInteger();
        final AtomicReference<Thread> threadConfirmacao = new AtomicReference<>();
        final AtomicReference<Thread> threadCancelamento = new AtomicReference<>();
        boolean falharConfirmacao;
        boolean falharCancelamento;

        public void agendamentoConfirmado(AgendamentoConfirmado evento) {
            confirmados.incrementAndGet();
            threadConfirmacao.set(Thread.currentThread());
            if (falharConfirmacao) throw new IllegalStateException("Falha simulada na confirmacao");
        }

        public void agendamentoCancelado(AgendamentoCancelado evento) {
            cancelados.incrementAndGet();
            threadCancelamento.set(Thread.currentThread());
            if (falharCancelamento) throw new IllegalStateException("Falha simulada no cancelamento");
        }

        void reset() {
            confirmados.set(0);
            cancelados.set(0);
            threadConfirmacao.set(null);
            threadCancelamento.set(null);
            falharConfirmacao = false;
            falharCancelamento = false;
        }
    }

    static class NotificadorQueFalha implements NotificadorDeAgendamento {
        public void agendamentoConfirmado(AgendamentoConfirmado evento) {
            throw new IllegalStateException("falha simulada na confirmacao");
        }

        public void agendamentoCancelado(AgendamentoCancelado evento) {
            throw new IllegalStateException("falha simulada no cancelamento");
        }
    }

    private record Conta(UUID id, String token) {}
}
