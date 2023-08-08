package com.msik404.karmaapp.post;

import com.msik404.karmaapp.karma.KarmaScoreAlreadyExistsException;
import com.msik404.karmaapp.karma.KarmaScoreNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class PostControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(KarmaScoreAlreadyExistsException.class)
    public ProblemDetail karmaScoreAlreadyExistsException(KarmaScoreAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(KarmaScoreNotFoundException.class)
    public ProblemDetail karmaScoreNotFoundException(KarmaScoreNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ProblemDetail postNotFoundException(PostNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(FileProcessingException.class)
    public ProblemDetail fileProcessingException(FileProcessingException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

}
