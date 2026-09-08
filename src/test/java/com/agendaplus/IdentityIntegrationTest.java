package com.agendaplus;

import com.agendaplus.identity.domain.repository.UsuarioRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.agendaplus.identity.domain.model.Email;
import com.agendaplus.identity.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class IdentityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired UsuarioRepository usuarios;
    @Value("${security.jwt.secret}") String segredo;

    @Test
    void cadastraClientePersisteHashEFazLoginComTokenValido() throws Exception {
        String email = email();
        cadastrar("/auth/cadastro", email).andExpect(status().isCreated())
                .andExpect(jsonPath("role").value("CLIENTE"))
                .andExpect(jsonPath("senhaHash").doesNotExist())
                .andExpect(jsonPath("senha").doesNotExist());
        String hash = jdbc.queryForObject("select senha_hash from usuarios where email = ?", String.class, email);
        assertThat(hash).isNotEqualTo("senha-segura");
        assertThat(new BCryptPasswordEncoder().matches("senha-segura", hash)).isTrue();
        var login = enviar("/auth/login", Map.of("email", email.toUpperCase(), "senha", "senha-segura"))
                .andExpect(status().isOk()).andExpect(jsonPath("tokenType").value("Bearer"))
                .andReturn();
        String token = json.readTree(login.getResponse().getContentAsString()).get("token").asText();
        mvc.perform(get("/identity/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("email").value(email))
                .andExpect(jsonPath("role").value("CLIENTE"))
                .andExpect(jsonPath("senhaHash").doesNotExist());
    }

    @Test
    void cadastroNegocioSempreCriaAdmin() throws Exception {
        String email = email();
        cadastrar("/auth/cadastro-negocio", email).andExpect(status().isCreated())
                .andExpect(jsonPath("role").value("ADMIN"));
        assertThat(jdbc.queryForObject("select role from usuarios where email = ?", String.class, email)).isEqualTo("ADMIN");
    }

    @Test
    void rejeitaEscolhaDeRoleNosDoisCadastros() throws Exception {
        for (String rota : new String[]{"/auth/cadastro", "/auth/cadastro-negocio"}) {
            String email = email();
            enviar(rota, Map.of("nome", "Ana", "email", email, "senha", "senha-segura", "role", "PROFISSIONAL"))
                    .andExpect(status().isBadRequest());
            assertThat(jdbc.queryForObject("select count(*) from usuarios where email = ?", Integer.class, email)).isZero();
        }
    }

    @Test
    void emailDuplicadoEntreCadastrosRetornaConflito() throws Exception {
        String email = email();
        cadastrar("/auth/cadastro", email).andExpect(status().isCreated());
        cadastrar("/auth/cadastro-negocio", " " + email.toUpperCase() + " ")
                .andExpect(status().isConflict());
    }

    @Test
    void credenciaisIncorretasRetornam401SemDistinguirContaInexistente() throws Exception {
        String email = email();
        cadastrar("/auth/cadastro", email).andExpect(status().isCreated());
        String existente = enviar("/auth/login", Map.of("email", email, "senha", "senha-incorreta"))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        String ausente = enviar("/auth/login", Map.of("email", email(), "senha", "senha-incorreta"))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        assertThat(json.readTree(existente).get("detail")).isEqualTo(json.readTree(ausente).get("detail"));
    }

    @Test
    void rejeitaCadastroInvalido() throws Exception {
        enviar("/auth/cadastro", Map.of("nome", " ", "email", "invalido", "senha", ""))
                .andExpect(status().isBadRequest());
        enviar("/auth/cadastro", Map.of("nome", "Ana", "email", email(), "senha", "á".repeat(37)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rotaProtegidaRejeitaTokenAusenteOuInvalido() throws Exception {
        mvc.perform(get("/identity/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/identity/me").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegidaRejeitaTokenExpiradoAssinaturaErradaEUsuarioRemovido() throws Exception {
        String email = email();
        cadastrar("/auth/cadastro", email).andExpect(status().isCreated());
        var usuario = usuarios.buscarPorEmail(new Email(email)).orElseThrow();
        var passado = new JwtService(segredo, Duration.ofHours(1), Clock.offset(Clock.systemUTC(), Duration.ofHours(-2)));
        var outraChave = new JwtService("outra-chave-exclusiva-para-testes-987654321", Duration.ofHours(1), Clock.systemUTC());
        for (String token : new String[]{passado.gerar(usuario).token(), outraChave.gerar(usuario).token()}) {
            mvc.perform(get("/identity/me").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }
        String token = new JwtService(segredo, Duration.ofHours(1), Clock.systemUTC()).gerar(usuario).token();
        jdbc.update("delete from usuarios where id = ?", usuario.getId());
        mvc.perform(get("/identity/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions cadastrar(String rota, String email) throws Exception {
        return enviar(rota, Map.of("nome", "Ana Silva", "email", email, "senha", "senha-segura"));
    }

    private ResultActions enviar(String rota, Object corpo) throws Exception {
        return mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(corpo)));
    }

    private String email() { return UUID.randomUUID() + "@exemplo.com"; }
}
