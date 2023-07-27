package com.msik404.karmaapp;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class KarmaAppControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail dataIntegrityViolationException(DataIntegrityViolationException ex) {

        String detail = "DataIntegrityViolationException";
        String[] values = ex.getMostSpecificCause().getMessage().split("Detail: ");
        if (values.length > 1) {
            detail = values[1];
        }
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
    }

}
