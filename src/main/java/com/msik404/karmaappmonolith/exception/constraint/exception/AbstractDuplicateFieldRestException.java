package com.msik404.karmaappmonolith.exception.constraint.exception;

import com.msik404.karmaappmonolith.exception.AbstractRestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public abstract class AbstractDuplicateFieldRestException extends AbstractRestException {

    public AbstractDuplicateFieldRestException(String errorMessage) {
        super(errorMessage);
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, getMessage());
    }

}
