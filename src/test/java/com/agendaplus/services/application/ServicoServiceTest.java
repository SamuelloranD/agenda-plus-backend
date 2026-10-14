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
import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        when(servicos.findById(id)).thenReturn(Optional.empty());

        assertThrows(ServicoNaoEncontradoException.class, () -> service.excluir(id));
    }

    @Test
    void desativaServicoComHistoricoParaPreservarAgendamentos() {
        UUID id = UUID.randomUUID();
        var servico = new com.agendaplus.services.domain.model.Servico(
                id, "Barba", 30,
                new com.agendaplus.shared.domain.model.Dinheiro(
                        new BigDecimal("50.00"), Currency.getInstance("BRL")));
        when(servicos.findById(id)).thenReturn(Optional.of(servico));
        when(agendamentos.temVinculosServico(id)).thenReturn(true);
        when(agendamentos.temVinculosServicoAtivosOuFuturos(id)).thenReturn(false);

        assertDoesNotThrow(() -> service.excluir(id));
        assertFalse(servico.isAtivo());
        verify(servicos).saveAndFlush(servico);
        verify(servicos, never()).deleteById(id);
    }

    @Test
    void bloqueiaExclusaoQuandoHaAgendamentoPendenteOuConfirmadoEmAberto() {
        UUID id = UUID.randomUUID();
        when(servicos.findById(id)).thenReturn(Optional.of(new com.agendaplus.services.domain.model.Servico(
                id, "Barba", 30,
                new com.agendaplus.shared.domain.model.Dinheiro(
                        new BigDecimal("50.00"), Currency.getInstance("BRL")))));
        when(agendamentos.temVinculosServicoAtivosOuFuturos(id)).thenReturn(true);

        assertThrows(ServicoComAgendamentosException.class, () -> service.excluir(id));
        verify(servicos, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
        verify(servicos, never()).deleteById(id);
    }
}
