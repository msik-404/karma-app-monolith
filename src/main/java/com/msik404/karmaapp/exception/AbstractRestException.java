package com.msik404.karmaapp.exception;

public abstract class AbstractRestException extends RuntimeException implements RestException {

    public AbstractRestException(String errorMessage) {
        super(errorMessage);
    }

}
