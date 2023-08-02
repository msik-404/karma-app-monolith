package com.msik404.karmaapp.chainHandler;

/**
 * Chain of responsibility
 */
public interface ChainHandler<T> {

    void setNext(ChainHandler<T> handler);

    void handle(T request);

}
