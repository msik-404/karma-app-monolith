package com.msik404.karmaapp.exception.constraint.chain;

public interface ChainExceptionHandler {

    void setNext(ChainExceptionHandler handler);

    void handle(String fieldName, String errorMessage);

}
