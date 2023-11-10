package com.msik404.karmaappmonolith.post.exception;

import com.msik404.karmaappmonolith.exception.AbstractRestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class PostNotFoundException extends AbstractRestException {

    public static final String ERROR_MESSAGE = "Post with that id was not found.";

    public PostNotFoundException() {
        super(ERROR_MESSAGE);
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, getMessage());
    }

}
