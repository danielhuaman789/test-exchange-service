package pe.test.exchange.application.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.test.exchange.domain.exception.ExchangeRateUnavailableException;
import pe.test.exchange.domain.exception.UnsupportedCurrencyException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            IllegalArgumentException.class,
            UnsupportedCurrencyException.class
    })
    public ProblemDetail handleInvalidRequest(RuntimeException exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Solicitud inválida",
                exception.getMessage()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleInvalidJson(
            HttpMessageNotReadableException exception
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Solicitud inválida",
                "El cuerpo debe ser un JSON válido con los tipos de datos esperados"
        );
    }

    @ExceptionHandler(ExchangeRateUnavailableException.class)
    public ProblemDetail handleProviderError(
            ExchangeRateUnavailableException exception
    ) {
        return problem(
                HttpStatus.BAD_GATEWAY,
                "Error del proveedor",
                exception.getMessage()
        );
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handlePersistenceError(
            DataAccessException exception
    ) {
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error de persistencia",
                "No se pudo guardar la conversión"
        );
    }

    private ProblemDetail problem(
            HttpStatus status,
            String title,
            String detail
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                status, detail
        );
        problem.setTitle(title);
        return problem;
    }
}
