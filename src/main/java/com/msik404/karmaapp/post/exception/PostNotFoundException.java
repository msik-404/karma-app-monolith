package com.msik404.karmaapp.post.exception;

import com.msik404.karmaapp.exception.AbstractRestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class PostNotFoundException extends AbstractRestException {

    private static final String ERROR_MESSAGE = "Post with that id was not found";

    public PostNotFoundException() {
        super(ERROR_MESSAGE);
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, getMessage());
    }

}
