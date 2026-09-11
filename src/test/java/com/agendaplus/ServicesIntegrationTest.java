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

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ServicesIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void realizaCrudDeServicoComPrecoMonetario() throws Exception {
        String token = token();
        var request = Map.of("nome", "Corte masculino", "duracaoMinutos", 60,
                "preco", Map.of("valor", "45.00", "moeda", "BRL"));

        String body = mvc.perform(post("/servicos").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("nome").value("Corte masculino"))
                .andExpect(jsonPath("preco.valor").value(45.00))
                .andExpect(jsonPath("preco.moeda").value("BRL"))
                .andReturn().getResponse().getContentAsString();
        String id = json.readTree(body).get("id").asText();

        mvc.perform(get("/servicos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("[0].id").value(id));
        mvc.perform(get("/servicos/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("duracaoMinutos").value(60));

        var update = Map.of("nome", "Corte premium", "duracaoMinutos", 75,
                "preco", Map.of("valor", "60.00", "moeda", "BRL"));
        mvc.perform(put("/servicos/{id}", id).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(update)))
                .andExpect(status().isOk()).andExpect(jsonPath("duracaoMinutos").value(75));
        mvc.perform(delete("/servicos/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void rejeitaDuracaoNaoPositivaEPrecoNegativo() throws Exception {
        String token = token();
        var request = Map.of("nome", "Inválido", "duracaoMinutos", 0,
                "preco", Map.of("valor", "-1.00", "moeda", "BRL"));

        mvc.perform(post("/servicos").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest());
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
