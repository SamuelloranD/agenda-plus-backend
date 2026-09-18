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
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class SchedulingIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void criaAgendamentoComSucessoEListaPorPeriodo() throws Exception {
        Cliente cliente = cliente();
        LocalDateTime inicio = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
        var request = request(inicio, cliente.id());

        mvc.perform(post("/agendamentos").header("Authorization", "Bearer " + cliente.token())
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(request)))
                .andExpect(status().isCreated()).andExpect(jsonPath("status").value("PENDENTE"));

        mvc.perform(get("/agendamentos").header("Authorization", "Bearer " + cliente.token())
                        .param("dataInicio", inicio.toLocalDate().toString()).param("dataFim", inicio.toLocalDate().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("totalElementos").value(1));
    }

    @Test
    void rejeitaCriacaoComConflitoDeHorario() throws Exception {
        Cliente cliente = cliente();
        LocalDateTime inicio = LocalDateTime.now().plusDays(3).withSecond(0).withNano(0);
        var request = request(inicio, cliente.id());
        criar(cliente.token(), request).andExpect(status().isCreated());
        criar(cliente.token(), request).andExpect(status().isConflict());
    }

    @Test
    void permiteCancelarForaDaJanelaERejeitaDentroDaJanelaDe24Horas() throws Exception {
        Cliente cliente = cliente();
        LocalDateTime fora = LocalDateTime.now().plusDays(3).withSecond(0).withNano(0);
        String idFora = id(criar(cliente.token(), request(fora, cliente.id())).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        mvc.perform(patch("/agendamentos/{id}/cancelar", idFora).header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isOk()).andExpect(jsonPath("status").value("CANCELADO"));

        LocalDateTime dentro = LocalDateTime.now().plusHours(12).withSecond(0).withNano(0);
        String idDentro = id(criar(cliente.token(), request(dentro, cliente.id())).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        mvc.perform(patch("/agendamentos/{id}/cancelar", idDentro).header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaCancelamentoDeStatusNaoCancelavel() throws Exception {
        Cliente cliente = cliente();
        LocalDateTime inicio = LocalDateTime.now().plusDays(3).withSecond(0).withNano(0);
        String agendamentoId = id(criar(cliente.token(), request(inicio, cliente.id()))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());

        mvc.perform(patch("/agendamentos/{id}/cancelar", agendamentoId)
                        .header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isOk());
        mvc.perform(patch("/agendamentos/{id}/cancelar", agendamentoId)
                        .header("Authorization", "Bearer " + cliente.token()))
                .andExpect(status().isBadRequest());
    }

    private org.springframework.test.web.servlet.ResultActions criar(String token, Map<String, Object> request) throws Exception {
        return mvc.perform(post("/agendamentos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(request)));
    }

    private Map<String, Object> request(LocalDateTime inicio, UUID clienteId) {
        return Map.of("inicio", inicio, "fim", inicio.plusHours(1), "profissionalId", UUID.randomUUID(),
                "clienteId", clienteId, "servicoId", UUID.randomUUID());
    }

    private String id(String body) throws Exception { return json.readTree(body).get("id").asText(); }

    private Cliente cliente() throws Exception {
        String email = UUID.randomUUID() + "@exemplo.com";
        String cadastro = mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("nome", "Cliente", "email", email, "senha", "senha-segura"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("email", email, "senha", "senha-segura"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode token = json.readTree(body).get("token");
        UUID id = UUID.fromString(json.readTree(cadastro).get("id").asText());
        return new Cliente(id, token.asText());
    }

    private record Cliente(UUID id, String token) {}
}
