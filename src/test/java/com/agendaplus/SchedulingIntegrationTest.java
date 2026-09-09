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
        String token = token();
        LocalDateTime inicio = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
        var request = request(inicio);

        mvc.perform(post("/agendamentos").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(request)))
                .andExpect(status().isCreated()).andExpect(jsonPath("status").value("PENDENTE"));

        mvc.perform(get("/agendamentos").header("Authorization", "Bearer " + token)
                        .param("dataInicio", inicio.toLocalDate().toString()).param("dataFim", inicio.toLocalDate().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("totalElementos").value(1));
    }

    @Test
    void rejeitaCriacaoComConflitoDeHorario() throws Exception {
        String token = token();
        LocalDateTime inicio = LocalDateTime.now().plusDays(3).withSecond(0).withNano(0);
        var request = request(inicio);
        criar(token, request).andExpect(status().isCreated());
        criar(token, request).andExpect(status().isConflict());
    }

    @Test
    void permiteCancelarForaDaJanelaERejeitaDentroDaJanelaDe24Horas() throws Exception {
        String token = token();
        LocalDateTime fora = LocalDateTime.now().plusDays(3).withSecond(0).withNano(0);
        String idFora = id(criar(token, request(fora)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        mvc.perform(patch("/agendamentos/{id}/cancelar", idFora).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("status").value("CANCELADO"));

        LocalDateTime dentro = LocalDateTime.now().plusHours(12).withSecond(0).withNano(0);
        String idDentro = id(criar(token, request(dentro)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        mvc.perform(patch("/agendamentos/{id}/cancelar", idDentro).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    private org.springframework.test.web.servlet.ResultActions criar(String token, Map<String, Object> request) throws Exception {
        return mvc.perform(post("/agendamentos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(request)));
    }

    private Map<String, Object> request(LocalDateTime inicio) {
        return Map.of("inicio", inicio, "fim", inicio.plusHours(1), "profissionalId", UUID.randomUUID(),
                "clienteId", UUID.randomUUID(), "servicoId", UUID.randomUUID());
    }

    private String id(String body) throws Exception { return json.readTree(body).get("id").asText(); }

    private String token() throws Exception {
        String email = UUID.randomUUID() + "@exemplo.com";
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("nome", "Cliente", "email", email, "senha", "senha-segura"))))
                .andExpect(status().isCreated());
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("email", email, "senha", "senha-segura"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode token = json.readTree(body).get("token");
        return token.asText();
    }
}
