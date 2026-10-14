package com.agendaplus.professionals.application;

import com.agendaplus.professionals.infrastructure.persistence.ProfissionalRepository;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ProfissionalServiceTest {
    @Test
    void recusaExclusaoQuandoExisteHistoricoDeAgendamentos() {
        var id = UUID.randomUUID();
        var profissionais = mock(ProfissionalRepository.class);
        var agendamentos = mock(AgendamentoRepository.class);
        when(profissionais.existsById(id)).thenReturn(true);
        when(agendamentos.temVinculosProfissional(id)).thenReturn(true);

        assertThatThrownBy(() -> new ProfissionalService(profissionais, agendamentos).excluir(id))
                .isInstanceOf(ProfissionalComAgendamentosException.class);
        verify(profissionais, never()).deleteById(id);
    }
}
