package com.msik404.karmaappmonolith.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public interface RestException {

    @NonNull
    ProblemDetail getProblemDetail();

}
