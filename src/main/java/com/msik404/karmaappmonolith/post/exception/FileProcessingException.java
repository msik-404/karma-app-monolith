package com.msik404.karmaappmonolith.post.exception;

import com.msik404.karmaappmonolith.exception.AbstractRestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class FileProcessingException extends AbstractRestException {

    public static final String ERROR_MESSAGE = "File could not be processed for some reason.";

    public FileProcessingException() {
        super(ERROR_MESSAGE);
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, getMessage());
    }

}
