package com.agendaplus;

import com.agendaplus.identity.domain.model.Role;
import com.agendaplus.identity.infrastructure.persistence.SpringDataUsuarioRepository;
import com.agendaplus.identity.infrastructure.persistence.UsuarioJpaEntity;
import com.agendaplus.professionals.domain.model.Profissional;
import com.agendaplus.professionals.infrastructure.persistence.ProfissionalRepository;
import com.agendaplus.scheduling.domain.model.StatusAgendamento;
import com.agendaplus.scheduling.infrastructure.persistence.AgendamentoJpaEntity;
import com.agendaplus.scheduling.infrastructure.persistence.SpringDataAgendamentoRepository;
import com.agendaplus.services.domain.model.Servico;
import com.agendaplus.services.infrastructure.persistence.ServicoRepository;
import com.agendaplus.shared.domain.model.Dinheiro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuditoriaIntegrationTest {
    private static final Instant UPDATED_AT_ANTIGO = Instant.parse("2000-01-01T00:00:00Z");

    @Autowired SpringDataUsuarioRepository usuarios;
    @Autowired SpringDataAgendamentoRepository agendamentos;
    @Autowired ServicoRepository servicos;
    @Autowired ProfissionalRepository profissionais;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired Environment environment;

    @Test
    void migrationCriaAuditoriaTimestamptzNaoNulaComDefaultEJpaValidaSchema() {
        var colunas = jdbcTemplate.query("""
                SELECT table_name, column_name, data_type, is_nullable, column_default
                  FROM information_schema.columns
                 WHERE table_schema = 'public'
                   AND table_name IN ('usuarios', 'agendamentos', 'servicos', 'profissionais')
                   AND column_name IN ('created_at', 'updated_at')
                 ORDER BY table_name, column_name
                """, (resultado, linha) -> new ColunaAuditoria(
                resultado.getString("table_name"),
                resultado.getString("column_name"),
                resultado.getString("data_type"),
                resultado.getString("is_nullable"),
                resultado.getString("column_default") != null));

        assertEquals(List.of(
                new ColunaAuditoria("agendamentos", "created_at", "timestamp with time zone", "NO", true),
                new ColunaAuditoria("agendamentos", "updated_at", "timestamp with time zone", "NO", true),
                new ColunaAuditoria("profissionais", "created_at", "timestamp with time zone", "NO", true),
                new ColunaAuditoria("profissionais", "updated_at", "timestamp with time zone", "NO", true),
                new ColunaAuditoria("servicos", "created_at", "timestamp with time zone", "NO", true),
                new ColunaAuditoria("servicos", "updated_at", "timestamp with time zone", "NO", true),
                new ColunaAuditoria("usuarios", "created_at", "timestamp with time zone", "NO", true),
                new ColunaAuditoria("usuarios", "updated_at", "timestamp with time zone", "NO", true)
        ), colunas);
        assertEquals("validate", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
    }

    @Test
    void preenchePreservaCreatedAtEAvancaUpdatedAtNoUsuario() {
        var id = UUID.randomUUID();
        usuarios.saveAndFlush(new UsuarioJpaEntity(id, "Ana", emailAleatorio(), "x".repeat(60), Role.CLIENTE));

        var criado = usuarios.findById(id).orElseThrow();
        var createdAt = auditoriaInicial(criado.getCreatedAt(), criado.getUpdatedAt());
        fixarUpdatedAtNoPassado("usuarios", id);

        var paraAtualizar = usuarios.findById(id).orElseThrow();
        adulterarCreatedAt(paraAtualizar, createdAt);
        paraAtualizar.atualizar("Ana Atualizada", emailAleatorio(), "y".repeat(60), Role.CLIENTE);
        usuarios.saveAndFlush(paraAtualizar);

        var atualizado = usuarios.findById(id).orElseThrow();
        assertAuditoriaAposAtualizacao(createdAt, atualizado.getCreatedAt(), atualizado.getUpdatedAt());
    }

    @Test
    void preenchePreservaCreatedAtEAvancaUpdatedAtNoAgendamento() {
        var id = UUID.randomUUID();
        var inicio = LocalDateTime.now().plusDays(3);
        agendamentos.saveAndFlush(new AgendamentoJpaEntity(id, inicio, inicio.plusHours(1),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), StatusAgendamento.PENDENTE));

        var criado = agendamentos.findById(id).orElseThrow();
        var createdAt = auditoriaInicial(criado.getCreatedAt(), criado.getUpdatedAt());
        fixarUpdatedAtNoPassado("agendamentos", id);

        var paraAtualizar = agendamentos.findById(id).orElseThrow();
        adulterarCreatedAt(paraAtualizar, createdAt);
        paraAtualizar.atualizarStatus(StatusAgendamento.CONFIRMADO);
        agendamentos.saveAndFlush(paraAtualizar);

        var atualizado = agendamentos.findById(id).orElseThrow();
        assertAuditoriaAposAtualizacao(createdAt, atualizado.getCreatedAt(), atualizado.getUpdatedAt());
    }

    @Test
    void preenchePreservaCreatedAtEAvancaUpdatedAtNoServico() {
        var id = UUID.randomUUID();
        servicos.saveAndFlush(new Servico(id, "Corte", 30, dinheiro("50.00")));

        var criado = servicos.findById(id).orElseThrow();
        var createdAt = auditoriaInicial(criado.getCreatedAt(), criado.getUpdatedAt());
        fixarUpdatedAtNoPassado("servicos", id);

        var paraAtualizar = servicos.findById(id).orElseThrow();
        adulterarCreatedAt(paraAtualizar, createdAt);
        paraAtualizar.atualizar("Corte premium", 45, dinheiro("75.00"));
        servicos.saveAndFlush(paraAtualizar);

        var atualizado = servicos.findById(id).orElseThrow();
        assertAuditoriaAposAtualizacao(createdAt, atualizado.getCreatedAt(), atualizado.getUpdatedAt());
    }

    @Test
    void preenchePreservaCreatedAtEAvancaUpdatedAtNoProfissional() {
        var id = UUID.randomUUID();
        profissionais.saveAndFlush(new Profissional(id, "Ana", "Corte"));

        var criado = profissionais.findById(id).orElseThrow();
        var createdAt = auditoriaInicial(criado.getCreatedAt(), criado.getUpdatedAt());
        fixarUpdatedAtNoPassado("profissionais", id);

        var paraAtualizar = profissionais.findById(id).orElseThrow();
        adulterarCreatedAt(paraAtualizar, createdAt);
        paraAtualizar.atualizarDados("Ana Atualizada", "Coloracao");
        profissionais.saveAndFlush(paraAtualizar);

        var atualizado = profissionais.findById(id).orElseThrow();
        assertAuditoriaAposAtualizacao(createdAt, atualizado.getCreatedAt(), atualizado.getUpdatedAt());
    }

    private Instant auditoriaInicial(Instant createdAt, Instant updatedAt) {
        assertNotNull(createdAt);
        assertNotNull(updatedAt);
        return createdAt;
    }

    private void adulterarCreatedAt(Object entidade, Instant createdAtOriginal) {
        ReflectionTestUtils.setField(entidade, "createdAt", createdAtOriginal.plus(1, ChronoUnit.DAYS));
    }

    private void fixarUpdatedAtNoPassado(String tabela, UUID id) {
        String sql = switch (tabela) {
            case "usuarios" -> "UPDATE usuarios SET updated_at = TIMESTAMPTZ '2000-01-01 00:00:00+00' WHERE id = ?";
            case "agendamentos" -> "UPDATE agendamentos SET updated_at = TIMESTAMPTZ '2000-01-01 00:00:00+00' WHERE id = ?";
            case "servicos" -> "UPDATE servicos SET updated_at = TIMESTAMPTZ '2000-01-01 00:00:00+00' WHERE id = ?";
            case "profissionais" -> "UPDATE profissionais SET updated_at = TIMESTAMPTZ '2000-01-01 00:00:00+00' WHERE id = ?";
            default -> throw new IllegalArgumentException("Tabela fora do escopo da auditoria: " + tabela);
        };
        assertEquals(1, jdbcTemplate.update(sql, id));
    }

    private void assertAuditoriaAposAtualizacao(Instant createdAtOriginal, Instant createdAtAtual,
                                                 Instant updatedAtAtual) {
        assertEquals(createdAtOriginal, createdAtAtual);
        assertTrue(updatedAtAtual.isAfter(UPDATED_AT_ANTIGO),
                () -> "updatedAt nao avancou: " + updatedAtAtual);
    }

    private Dinheiro dinheiro(String valor) {
        return new Dinheiro(new BigDecimal(valor), Currency.getInstance("BRL"));
    }

    private String emailAleatorio() {
        return UUID.randomUUID() + "@example.com";
    }

    private record ColunaAuditoria(String tabela, String coluna, String tipo, String anulavel,
                                   boolean possuiDefault) {}
}
