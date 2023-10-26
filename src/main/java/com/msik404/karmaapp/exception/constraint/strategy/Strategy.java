package com.msik404.karmaapp.exception.constraint.strategy;

public interface Strategy<T, U> {

    U execute(T request);

}
