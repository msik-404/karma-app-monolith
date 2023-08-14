package com.msik404.karmaapp.constraintExceptions;

import com.msik404.karmaapp.chainHandler.ChainHandler;
import com.msik404.karmaapp.pair.Pair;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
public abstract class BaseExceptionHandler implements ChainHandler<Pair<String, String>> {

    @Nullable
    protected ChainHandler<Pair<String, String>> nextHandler;

    @Override
    public void setNext(@Nullable ChainHandler<Pair<String, String>> handler) {
        this.nextHandler = handler;
    }

}
