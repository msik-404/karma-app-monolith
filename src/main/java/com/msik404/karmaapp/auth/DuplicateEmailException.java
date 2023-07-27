package com.msik404.karmaapp.auth;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("This email is already used");
    }
}
