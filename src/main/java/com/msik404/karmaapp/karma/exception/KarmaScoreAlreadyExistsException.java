package com.msik404.karmaapp.karma.exception;

import com.msik404.karmaapp.exception.AbstractRestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class KarmaScoreAlreadyExistsException extends AbstractRestException {

    public KarmaScoreAlreadyExistsException(String message) {
        super(message);
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, getMessage());
    }

}
