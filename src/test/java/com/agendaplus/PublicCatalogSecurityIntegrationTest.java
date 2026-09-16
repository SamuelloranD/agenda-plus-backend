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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class PublicCatalogSecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void permiteLeituraPublicaDoCatalogoEDisponibilidade() throws Exception {
        CatalogData catalog = catalogData();

        mvc.perform(get("/servicos"))
                .andExpect(status().isOk());
        mvc.perform(get("/servicos/{id}", catalog.serviceId()))
                .andExpect(status().isOk());
        mvc.perform(get("/profissionais"))
                .andExpect(status().isOk());
        mvc.perform(get("/profissionais/{id}", catalog.professionalId()))
                .andExpect(status().isOk());
        mvc.perform(get("/profissionais/{id}/horarios-disponiveis", catalog.professionalId())
                        .param("data", "2026-09-21")
                        .param("servicoId", catalog.serviceId()))
                .andExpect(status().isOk());
    }

    @Test
    void mantémEscritasProtegidasMesmoComLeituraPublica() throws Exception {
        String professionalBody = json.writeValueAsString(Map.of(
                "nome", "Ana",
                "especialidade", "Corte",
                "horariosTrabalho", List.of(Map.of("diaSemana", "MONDAY", "inicio", "09:00", "fim", "12:00"))));
        String serviceBody = json.writeValueAsString(Map.of(
                "nome", "Corte",
                "duracaoMinutos", 60,
                "preco", Map.of("valor", "50.00", "moeda", "BRL")));

        mvc.perform(post("/servicos").contentType(MediaType.APPLICATION_JSON).content(serviceBody))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/servicos/{id}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(serviceBody))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/servicos/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/profissionais").contentType(MediaType.APPLICATION_JSON).content(professionalBody))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/profissionais/{id}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(professionalBody))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/profissionais/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/agendamentos").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                        "inicio", "2026-09-21T10:00:00",
                        "fim", "2026-09-21T11:00:00",
                        "profissionalId", UUID.randomUUID(),
                        "clienteId", UUID.randomUUID(),
                        "servicoId", UUID.randomUUID()))))
                .andExpect(status().isUnauthorized());
        mvc.perform(patch("/agendamentos/{id}/confirmar", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private CatalogData catalogData() throws Exception {
        String adminToken = token();
        String professionalBody = mvc.perform(post("/profissionais")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of(
                                "nome", "Ana",
                                "especialidade", "Corte",
                                "horariosTrabalho", List.of(Map.of("diaSemana", "MONDAY", "inicio", "09:00", "fim", "12:00"))))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String professionalId = json.readTree(professionalBody).get("id").asText();

        String serviceBody = mvc.perform(post("/servicos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of(
                                "nome", "Corte",
                                "duracaoMinutos", 60,
                                "preco", Map.of("valor", "50.00", "moeda", "BRL")))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String serviceId = json.readTree(serviceBody).get("id").asText();
        return new CatalogData(professionalId, serviceId);
    }

    private String token() throws Exception {
        String email = UUID.randomUUID() + "@exemplo.com";
        mvc.perform(post("/auth/cadastro-negocio").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("nome", "Admin", "email", email, "senha", "senha-segura"))))
                .andExpect(status().isCreated());
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("email", email, "senha", "senha-segura"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode token = json.readTree(body).get("token");
        return token.asText();
    }

    private record CatalogData(String professionalId, String serviceId) { }
}
