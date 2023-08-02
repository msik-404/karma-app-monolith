package com.msik404.karmaapp.constraintExceptions;

import com.msik404.karmaapp.chainHandler.ChainHandler;
import com.msik404.karmaapp.pair.Pair;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseExceptionHandler implements ChainHandler<Pair<String, String>> {

    protected ChainHandler<Pair<String, String>> nextHandler;

    @Override
    public void setNext(ChainHandler<Pair<String, String>> handler) {
        this.nextHandler = handler;
    }

}
