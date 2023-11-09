package com.msik404.karmaapp.post.exception;

import com.msik404.karmaapp.exception.AbstractRestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class PostNotFoundOrClientIsNotOwnerException extends AbstractRestException {

    public static final String ERROR_MESSAGE = "Requested post was not found or you are not the owner.";

    public PostNotFoundOrClientIsNotOwnerException() {
        super(ERROR_MESSAGE);
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, getMessage());
    }

}
