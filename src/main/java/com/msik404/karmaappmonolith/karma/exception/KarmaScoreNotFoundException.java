package com.msik404.karmaappmonolith.karma.exception;

import com.msik404.karmaappmonolith.exception.AbstractRestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class KarmaScoreNotFoundException extends AbstractRestException {

    public static final String ERROR_MESSAGE = "KarmaScore with that id was not found.";

    public KarmaScoreNotFoundException() {
        super(ERROR_MESSAGE);
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, getMessage());
    }

}
