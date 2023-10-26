package com.msik404.karmaapp;

import com.msik404.karmaapp.exception.AbstractRestException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class KarmaAppControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AbstractRestException.class)
    public ProblemDetail abstractRestException(AbstractRestException ex) {
        return ex.getProblemDetail();
    }

}
