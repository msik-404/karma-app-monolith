package com.msik404.karmaappmonolith.exception.constraint.strategy;

public interface Strategy<T, U> {

    U execute(T request);

}
