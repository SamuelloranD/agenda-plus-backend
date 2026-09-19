package com.agendaplus.services.application;

import com.agendaplus.services.infrastructure.persistence.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {
    @Mock
    ServicoRepository servicos;

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
}
