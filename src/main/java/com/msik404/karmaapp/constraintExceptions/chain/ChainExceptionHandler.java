package com.msik404.karmaapp.constraintExceptions.chain;

public interface ChainExceptionHandler {

    void setNext(ChainExceptionHandler handler);

    void handle(String fieldName, String errorMessage);

}
