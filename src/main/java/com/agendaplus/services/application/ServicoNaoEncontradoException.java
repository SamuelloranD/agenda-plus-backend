package com.agendaplus.services.application;

public class ServicoNaoEncontradoException extends RuntimeException {
    public ServicoNaoEncontradoException() { super("Serviço não encontrado."); }
}
