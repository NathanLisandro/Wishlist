package com.wishlist.nathan.presentation;

import com.wishlist.nathan.application.excecao.ConflitoException;
import com.wishlist.nathan.application.excecao.CredenciaisInvalidasException;
import com.wishlist.nathan.application.excecao.RecursoNaoEncontradoException;
import com.wishlist.nathan.application.port.TokenSessaoPort;
import com.wishlist.nathan.domain.presente.PresenteEsgotadoException;
import com.wishlist.nathan.domain.rsvp.LimiteAcompanhantesException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

/**
 * Respostas de erro consistentes em {@code application/problem+json} (RFC 7807).
 */
@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

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

    /**
     * Catch-all de erros inesperados (500). Loga o stack completo com uma referência
     * curta para correlacionar com o log (ex.: nos logs do Railway), sem vazar detalhe
     * interno nem PII para o cliente.
     */
    @ExceptionHandler(Exception.class)
    ProblemDetail erroInterno(Exception e) {
        String ref = UUID.randomUUID().toString().substring(0, 8);
        log.error("Erro inesperado [ref={}]: {}", ref, e.getMessage(), e);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado. Referência: " + ref);
    }

    private ProblemDetail problema(HttpStatus status, String detalhe) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setDetail(detalhe);
        return pd;
    }
}
