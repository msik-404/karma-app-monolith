package com.msik404.karmaappmonolith.exception.constraint.chain;

public interface ChainExceptionHandler {

    void setNext(ChainExceptionHandler handler);

    void handle(String fieldName, String errorMessage);

}
