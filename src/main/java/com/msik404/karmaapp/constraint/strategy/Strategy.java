package com.msik404.karmaapp.constraint.strategy;

public interface Strategy<T, U> {

    U execute(T request);

}
