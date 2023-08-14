package com.msik404.karmaapp.constraintExceptions;

import java.util.Optional;

import org.springframework.lang.NonNull;

public abstract class BaseExceptionHandler implements ChainExceptionHandler {

    protected Optional<ChainExceptionHandler> nextHandler;

    @Override
    public void setNext(@NonNull ChainExceptionHandler handler) {
        this.nextHandler = Optional.of(handler);
    }

}
