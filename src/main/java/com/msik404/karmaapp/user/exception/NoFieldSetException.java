package com.msik404.karmaapp.user.exception;

import com.msik404.karmaapp.exception.AbstractRestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class NoFieldSetException extends AbstractRestException {

    private static final String ERROR_MESSAGE = "Zero fields were selected to be changed. Choose at least one field for update.";

    public NoFieldSetException() {
        super(ERROR_MESSAGE);
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, getMessage());
    }

}
