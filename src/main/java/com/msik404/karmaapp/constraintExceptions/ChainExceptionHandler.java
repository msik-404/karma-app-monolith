package com.msik404.karmaapp.constraintExceptions;

public interface ChainExceptionHandler {

    void setNext(ChainExceptionHandler handler);

    void handle(String fieldName, String errorMessage);

}
