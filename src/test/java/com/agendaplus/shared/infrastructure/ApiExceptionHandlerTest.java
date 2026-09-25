package com.agendaplus.shared.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {
    @Test
    void transformaConflitoDePersistenciaEmRespostaDeConflito() {
        var problem = new ApiExceptionHandler().conflitoDePersistencia(
                new DataIntegrityViolationException("duplicate key"));

        assertThat(problem.getStatus()).isEqualTo(409);
        assertThat(problem.getDetail()).contains("conflito");
    }
}
