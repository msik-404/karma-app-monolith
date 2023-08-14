package com.msik404.karmaapp.constraintExceptions;

import java.util.Optional;

import com.msik404.karmaapp.chainHandler.ChainHandler;
import com.msik404.karmaapp.pair.Pair;
import lombok.NonNull;

public abstract class BaseExceptionHandler implements ChainHandler<Pair<String, String>> {

    protected Optional<ChainHandler<Pair<String, String>>> nextHandler;

    @Override
    public void setNext(@NonNull ChainHandler<Pair<String, String>> handler) {
        this.nextHandler = Optional.of(handler);
    }

}
