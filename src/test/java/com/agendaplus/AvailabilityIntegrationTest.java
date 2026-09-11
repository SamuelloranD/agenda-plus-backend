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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AvailabilityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void retornaSlotsDoServicoExcluindoAgendamentoExistente() throws Exception {
        String adminToken = token("Admin", true);
        String clientEmail = UUID.randomUUID() + "@exemplo.com";
        String clientId = cadastrarCliente(clientEmail);
        LocalDate data = LocalDate.of(2026, 9, 14);

        String professionalBody = mvc.perform(post("/profissionais").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("nome", "Ana", "especialidade", "Corte",
                                "horariosTrabalho", List.of(Map.of("diaSemana", "MONDAY", "inicio", "09:00", "fim", "12:00"))))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String professionalId = json.readTree(professionalBody).get("id").asText();

        String serviceBody = mvc.perform(post("/servicos").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("nome", "Corte", "duracaoMinutos", 60,
                                "preco", Map.of("valor", "50.00", "moeda", "BRL")))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String serviceId = json.readTree(serviceBody).get("id").asText();

        mvc.perform(post("/agendamentos").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("inicio", data + "T10:00:00", "fim", data + "T11:00:00",
                                "profissionalId", professionalId, "clienteId", clientId, "servicoId", serviceId))))
                .andExpect(status().isCreated());

        mvc.perform(get("/profissionais/{id}/horarios-disponiveis", professionalId)
                        .header("Authorization", "Bearer " + adminToken)
                        .param("data", data.toString()).param("servicoId", serviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("length()").value(2))
                .andExpect(jsonPath("[0].inicio").value("2026-09-14T09:00:00"))
                .andExpect(jsonPath("[1].inicio").value("2026-09-14T11:00:00"));
    }

    private String cadastrarCliente(String email) throws Exception {
        String body = mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("nome", "Cliente", "email", email, "senha", "senha-segura"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("id").asText();
    }

    private String token(String nome, boolean admin) throws Exception {
        String email = UUID.randomUUID() + "@exemplo.com";
        String rota = admin ? "/auth/cadastro-negocio" : "/auth/cadastro";
        mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("nome", nome, "email", email, "senha", "senha-segura"))))
                .andExpect(status().isCreated());
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("email", email, "senha", "senha-segura"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode token = json.readTree(body).get("token");
        return token.asText();
    }
}
