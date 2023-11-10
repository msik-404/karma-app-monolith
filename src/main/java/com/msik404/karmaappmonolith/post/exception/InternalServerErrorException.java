package com.msik404.karmaappmonolith.post.exception;

import com.msik404.karmaappmonolith.exception.AbstractRestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class InternalServerErrorException extends AbstractRestException {

    public InternalServerErrorException(String message) {
        super(message);
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, getMessage());
    }

}
