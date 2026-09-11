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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ProfessionalsIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void realizaCrudDeProfissionalComFaixasDeTrabalho() throws Exception {
        String token = token();
        var request = Map.of(
                "nome", "Ana Silva",
                "especialidade", "Corte e escova",
                "horariosTrabalho", List.of(Map.of("diaSemana", "MONDAY", "inicio", "09:00", "fim", "12:00")));

        String body = mvc.perform(post("/profissionais").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("nome").value("Ana Silva"))
                .andExpect(jsonPath("horariosTrabalho[0].diaSemana").value("MONDAY"))
                .andReturn().getResponse().getContentAsString();
        String id = json.readTree(body).get("id").asText();

        mvc.perform(get("/profissionais").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("length()").value(org.hamcrest.Matchers.greaterThan(0)));
        mvc.perform(get("/profissionais/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("especialidade").value("Corte e escova"));

        var update = Map.of("nome", "Ana Atualizada", "especialidade", "Coloração",
                "horariosTrabalho", List.of(Map.of("diaSemana", "TUESDAY", "inicio", "10:00", "fim", "14:00")));
        mvc.perform(put("/profissionais/{id}", id).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(update)))
                .andExpect(status().isOk()).andExpect(jsonPath("nome").value("Ana Atualizada"));
        mvc.perform(delete("/profissionais/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/profissionais/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
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
}
