package com.msik404.karmaapp.exception.constraint.exception;

public class DuplicateUsernameException extends AbstractDuplicateFieldRestException {

    public DuplicateUsernameException(String message) {
        super(String.format("The username: (%s) is already used", message));
    }

}
