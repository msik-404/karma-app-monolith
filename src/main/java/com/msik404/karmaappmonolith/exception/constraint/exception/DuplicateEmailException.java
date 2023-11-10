package com.msik404.karmaappmonolith.exception.constraint.exception;

public class DuplicateEmailException extends AbstractDuplicateFieldRestException {

    public DuplicateEmailException(String message) {
        super(String.format("The email: (%s) is already used", message));
    }

}
