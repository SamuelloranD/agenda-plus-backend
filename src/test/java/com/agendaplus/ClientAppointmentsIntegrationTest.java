package com.agendaplus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ClientAppointmentsIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void clienteAutenticadoRecebeSomenteOsPropriosAgendamentos() throws Exception {
        Cliente clienteA = cliente("Cliente A");
        Cliente clienteB = cliente("Cliente B");
        LocalDateTime inicio = LocalDateTime.now().plusDays(5).withSecond(0).withNano(0);

        criarAgendamento(clienteA, inicio);
        criarAgendamento(clienteB, inicio.plusHours(2));

        mvc.perform(get("/agendamentos/meus")
                        .header("Authorization", "Bearer " + clienteA.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("conteudo", hasSize(1)))
                .andExpect(jsonPath("conteudo[0].clienteId").value(clienteA.id().toString()))
                .andExpect(jsonPath("conteudo[*].clienteId", not(hasItem(clienteB.id().toString()))))
                .andExpect(jsonPath("totalElementos").value(1));
    }

    @Test
    void aplicaFiltrosDePeriodoEPaginacaoAntesDeResponder() throws Exception {
        Cliente clienteA = cliente("Cliente A");
        Cliente clienteB = cliente("Cliente B");
        LocalDateTime inicio = LocalDateTime.now().plusDays(10).withSecond(0).withNano(0);

        criarAgendamento(clienteA, inicio);
        criarAgendamento(clienteA, inicio.plusDays(1));
        criarAgendamento(clienteA, inicio.plusDays(10));
        criarAgendamento(clienteB, inicio.plusHours(3));

        mvc.perform(get("/agendamentos/meus")
                        .header("Authorization", "Bearer " + clienteA.token())
                        .param("dataInicio", inicio.toLocalDate().toString())
                        .param("dataFim", inicio.plusDays(1).toLocalDate().toString())
                        .param("pagina", "1")
                        .param("tamanho", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("conteudo", hasSize(1)))
                .andExpect(jsonPath("conteudo[0].clienteId").value(clienteA.id().toString()))
                .andExpect(jsonPath("conteudo[0].inicio").value(
                        inicio.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))))
                .andExpect(jsonPath("pagina").value(1))
                .andExpect(jsonPath("tamanho").value(1))
                .andExpect(jsonPath("totalElementos").value(2))
                .andExpect(jsonPath("totalPaginas").value(2));
    }

    @Test
    void rejeitaConsultaSemToken() throws Exception {
        mvc.perform(get("/agendamentos/meus"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void respondePaginaVaziaComMetadadosCoerentes() throws Exception {
        Cliente cliente = cliente("Cliente sem agendamentos");

        mvc.perform(get("/agendamentos/meus")
                        .header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("conteudo", hasSize(0)))
                .andExpect(jsonPath("pagina").value(0))
                .andExpect(jsonPath("tamanho").value(20))
                .andExpect(jsonPath("totalElementos").value(0))
                .andExpect(jsonPath("totalPaginas").value(0));
    }

    @Test
    void clienteProprietarioCancelaAgendamentoFuturo() throws Exception {
        Cliente cliente = cliente("Cliente proprietario");
        String agendamentoId = criarAgendamento(cliente, LocalDateTime.now().plusDays(3));

        mvc.perform(patch("/agendamentos/{id}/cancelar", agendamentoId)
                        .header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("CANCELADO"));
    }

    @Test
    void clienteNaoProprietarioNaoCancelaAgendamento() throws Exception {
        Cliente proprietario = cliente("Cliente proprietario");
        Cliente outroCliente = cliente("Outro cliente");
        String agendamentoId = criarAgendamento(proprietario, LocalDateTime.now().plusDays(3));

        mvc.perform(patch("/agendamentos/{id}/cancelar", agendamentoId)
                        .header("Authorization", "Bearer " + outroCliente.token()))
                .andExpect(status().isForbidden());

        mvc.perform(get("/agendamentos/meus")
                        .header("Authorization", "Bearer " + proprietario.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("conteudo[?(@.id == '%s')].status", agendamentoId)
                        .value(hasItem("PENDENTE")));
    }

    @Test
    void administradorCancelaAgendamentoDeCliente() throws Exception {
        Cliente cliente = cliente("Cliente");
        Cliente administrador = administrador("Administrador");
        String agendamentoId = criarAgendamento(cliente, LocalDateTime.now().plusDays(3));

        mvc.perform(patch("/agendamentos/{id}/cancelar", agendamentoId)
                        .header("Authorization", "Bearer " + administrador.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("CANCELADO"))
                .andExpect(jsonPath("clienteId").value(cliente.id().toString()));
    }

    private String criarAgendamento(Cliente cliente, LocalDateTime inicio) throws Exception {
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

    private Cliente cliente(String nome) throws Exception {
        return conta(nome, "/auth/cadastro");
    }

    private Cliente administrador(String nome) throws Exception {
        return conta(nome, "/auth/cadastro-negocio");
    }

    private Cliente conta(String nome, String rotaCadastro) throws Exception {
        String email = UUID.randomUUID() + "@exemplo.com";
        String cadastro = mvc.perform(post(rotaCadastro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of(
                                "nome", nome,
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
        return new Cliente(UUID.fromString(usuario.get("id").asText()), autenticacao.get("token").asText());
    }

    private record Cliente(UUID id, String token) {}
}
