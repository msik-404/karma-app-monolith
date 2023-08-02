package com.msik404.karmaapp.strategy;

public interface Strategy<T, U> {

    U execute(T request);

}
