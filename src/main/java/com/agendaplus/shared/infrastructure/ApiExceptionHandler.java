package com.agendaplus.shared.infrastructure;

import com.agendaplus.identity.domain.exception.EmailJaCadastradoException;
import com.agendaplus.professionals.application.ProfissionalNaoEncontradoException;
import com.agendaplus.identity.application.cliente.ClienteNaoEncontradoException;

import com.agendaplus.identity.application.exception.CredenciaisInvalidasException;
import com.agendaplus.scheduling.domain.exception.AgendamentoNaoEncontradoException;
import com.agendaplus.scheduling.domain.exception.CancelamentoNaoPermitidoException;
import com.agendaplus.scheduling.domain.exception.HorarioIndisponivelException;
import com.agendaplus.scheduling.domain.exception.TransicaoInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ProfissionalNaoEncontradoException.class)
    ProblemDetail profissionalNaoEncontrado(ProfissionalNaoEncontradoException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    ProblemDetail clienteNaoEncontrado(ClienteNaoEncontradoException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(AgendamentoNaoEncontradoException.class)
    ProblemDetail agendamentoNaoEncontrado(AgendamentoNaoEncontradoException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(HorarioIndisponivelException.class)
    ProblemDetail horarioIndisponivel(HorarioIndisponivelException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler({CancelamentoNaoPermitidoException.class, TransicaoInvalidaException.class})
    ProblemDetail transicaoAgendamentoInvalida(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
    @ExceptionHandler(EmailJaCadastradoException.class)
    ProblemDetail emailDuplicado(EmailJaCadastradoException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    ProblemDetail credenciaisInvalidas(CredenciaisInvalidasException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail argumentoInvalido(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ProblemDetail requisicaoInvalida(Exception exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido. Verifique os campos obrigatórios e remova campos desconhecidos.");
    }
}
