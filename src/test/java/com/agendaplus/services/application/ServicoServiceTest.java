package com.agendaplus.services.application;

import com.agendaplus.services.infrastructure.persistence.ServicoRepository;
import com.agendaplus.scheduling.domain.repository.AgendamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {
    @Mock
    ServicoRepository servicos;
    @Mock
    AgendamentoRepository agendamentos;

    @InjectMocks
    ServicoService service;

    @Test
    void lancaExcecaoEspecificaAoBuscarServicoInexistente() {
        UUID id = UUID.randomUUID();
        when(servicos.findById(id)).thenReturn(Optional.empty());

        assertThrows(ServicoNaoEncontradoException.class, () -> service.buscar(id));
    }

    @Test
    void lancaExcecaoEspecificaAoExcluirServicoInexistente() {
        UUID id = UUID.randomUUID();
        when(servicos.existsById(id)).thenReturn(false);

        assertThrows(ServicoNaoEncontradoException.class, () -> service.excluir(id));
    }

    @Test
    void protegeServicoComAgendamentosContraExclusao() {
        UUID id = UUID.randomUUID();
        when(servicos.existsById(id)).thenReturn(true);
        when(agendamentos.temVinculosServico(id)).thenReturn(true);

        assertThrows(ServicoComAgendamentosException.class, () -> service.excluir(id));
        verify(servicos, never()).deleteById(id);
    }
}
