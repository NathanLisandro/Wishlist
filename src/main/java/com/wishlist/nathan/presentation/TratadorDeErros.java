package com.wishlist.nathan.presentation;

import com.wishlist.nathan.application.excecao.ConflitoException;
import com.wishlist.nathan.application.excecao.CredenciaisInvalidasException;
import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.TokenSessaoPort;
import com.wishlist.nathan.domain.presente.PresenteEsgotadoException;
import com.wishlist.nathan.domain.rsvp.LimiteAcompanhantesException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Respostas de erro consistentes em {@code application/problem+json} (RFC 7807).
 */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    ProblemDetail conflito(ConflitoException e) {
        return problema(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(PresenteEsgotadoException.class)
    ProblemDetail esgotado(PresenteEsgotadoException e) {
        return problema(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(LimiteAcompanhantesException.class)
    ProblemDetail limiteAcompanhantes(LimiteAcompanhantesException e) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler({CredenciaisInvalidasException.class, TokenSessaoPort.SessaoInvalidaException.class})
    ProblemDetail naoAutorizado(RuntimeException e) {
        return problema(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class,
            ConstraintViolationException.class})
    ProblemDetail requisicaoInvalida(Exception e) {
        return problema(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    private ProblemDetail problema(HttpStatus status, String detalhe) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setDetail(detalhe);
        return pd;
    }
}
