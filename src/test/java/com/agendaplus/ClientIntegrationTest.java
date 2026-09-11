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
class ClientIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void listaBuscaAtualizaEExcluiSomenteClientesSemExporSenha() throws Exception {
        String adminToken = cadastrarEAutenticar("Admin", true);
        String clienteEmail = UUID.randomUUID() + "@exemplo.com";
        String clienteId = cadastrarCliente(clienteEmail);

        mvc.perform(get("/clientes").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[?(@.id == '" + clienteId + "')].nome").value("Cliente"))
                .andExpect(jsonPath("[?(@.id == '" + clienteId + "')].senhaHash").doesNotExist())
                .andExpect(jsonPath("[?(@.id == '" + clienteId + "')].senha").doesNotExist());

        mvc.perform(get("/clientes/{id}", clienteId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("id").value(clienteId));

        mvc.perform(put("/clientes/{id}", clienteId).header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("nome", "Cliente Atualizado", "email", UUID.randomUUID() + "@exemplo.com"))))
                .andExpect(status().isOk()).andExpect(jsonPath("nome").value("Cliente Atualizado"));

        mvc.perform(delete("/clientes/{id}", clienteId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        mvc.perform(get("/clientes/{id}", clienteId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    private String cadastrarCliente(String email) throws Exception {
        String body = mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(Map.of("nome", "Cliente", "email", email, "senha", "senha-segura"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("id").asText();
    }

    private String cadastrarEAutenticar(String nome, boolean admin) throws Exception {
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
