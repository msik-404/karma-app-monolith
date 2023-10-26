package com.msik404.karmaapp.constraint.exception;

public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException(String message) {
        super(String.format("The username: (%s) is already used", message));
    }

}
