package com.msik404.karmaapp.karma.exception;

import com.msik404.karmaapp.exception.AbstractRestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class KarmaScoreNotFoundException extends AbstractRestException {

    public KarmaScoreNotFoundException() {
        super("KarmaScore with that id was not found");
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, getMessage());
    }

}
