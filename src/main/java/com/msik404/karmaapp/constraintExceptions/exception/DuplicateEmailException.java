package com.msik404.karmaapp.constraintExceptions.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(String.format("The email: (%s) is already used", message));
    }

}
