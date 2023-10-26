package com.msik404.karmaapp.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public interface RestException {

    @NonNull
    ProblemDetail getProblemDetail();

}
